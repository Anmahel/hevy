package com.example.gymtracker.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.data.local.entity.WorkoutSessionEntity
import com.example.gymtracker.data.local.entity.WorkoutSetEntity
import com.example.gymtracker.data.local.relation.WorkoutSessionWithDetails
import com.example.gymtracker.domain.model.Exercise
import com.example.gymtracker.domain.model.HypertrophyMuscle
import com.example.gymtracker.domain.model.Workout
import com.example.gymtracker.domain.model.WorkoutSet
import com.example.gymtracker.domain.model.WorkoutWithSets
import com.example.gymtracker.domain.repository.GymRepository
import com.example.gymtracker.domain.repository.RoutineRepository
import com.example.gymtracker.domain.repository.WorkoutRepository
import com.example.gymtracker.domain.util.PersonalRecordAchievement
import com.example.gymtracker.domain.util.RpgGamificationEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Estados explícitos de carga de datos para el entrenamiento activo:
 * - Loading: Room está emitiendo o el entreno se está inicializando.
 * - Success: Datos completos listos para renderizar.
 * - Error: Fallo al cargar o inicializar la sesión.
 */
sealed interface WorkoutDataState {
    data object Loading : WorkoutDataState
    data class Success(val workoutWithSets: WorkoutWithSets) : WorkoutDataState
    data class Error(val message: String) : WorkoutDataState
}

data class ExerciseSummaryItem(
    val exerciseId: Long,
    val exerciseName: String,
    val muscleGroup: String,
    val completedSetsCount: Int,
    val totalSetsCount: Int,
    val volumeKg: Double,
    val setsDescription: List<String>
)

data class WorkoutSummaryData(
    val workoutId: Long,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val totalVolumeKg: Double,
    val completedSetsCount: Int,
    val totalSetsCount: Int,
    val exercisesSummary: List<ExerciseSummaryItem>,
    val workedMuscles: Set<HypertrophyMuscle> = emptySet(),
    val newPersonalRecords: List<PersonalRecordAchievement> = emptyList(),
    val xpEarnedTotal: Int = RpgGamificationEngine.XP_PER_FINISHED_WORKOUT
)

data class ActiveWorkoutUiState(
    val dataState: WorkoutDataState = WorkoutDataState.Loading,
    val allAvailableExercises: List<Exercise> = emptyList(),
    val elapsedSeconds: Long = 0,
    val isAddExerciseDialogOpen: Boolean = false,
    val isWorkoutFinished: Boolean = false,
    val isSaving: Boolean = false,
    // Rest Timer state
    val isRestTimerActive: Boolean = false,
    val isRestTimerExpanded: Boolean = false,
    val restTimerTotalSeconds: Int = 90,
    val restTimerRemainingSeconds: Int = 90,
    // Summary Dialog state
    val isSummaryDialogOpen: Boolean = false,
    val workoutSummary: WorkoutSummaryData? = null,
    // Módulo Pro: Calculadora de discos y series de calentamiento
    val plateCalculatorExercise: Exercise? = null,
    val plateCalculatorInitialWeight: Double = 60.0,
    // Módulo Pro: Superseries y enfoque de ejercicio
    val supersetPairs: Map<Long, Long> = emptyMap(), // exerciseId -> pairedExerciseId
    val focusedExerciseId: Long? = null,
    val supersetLinkExercise: Exercise? = null,
    val supersetFeedbackMessage: String? = null
) {
    val isLoading: Boolean get() = dataState is WorkoutDataState.Loading
    val isError: Boolean get() = dataState is WorkoutDataState.Error
    val errorMessage: String? get() = (dataState as? WorkoutDataState.Error)?.message

    val workoutWithSets: WorkoutWithSets?
        get() = (dataState as? WorkoutDataState.Success)?.workoutWithSets

    val workout: Workout? get() = workoutWithSets?.workout

    // Backward compatibility getters
    val isTimerOpen: Boolean get() = isRestTimerActive
    val timerSeconds: Int get() = restTimerRemainingSeconds
}

class ActiveWorkoutViewModel(
    private val workoutId: Long = 0L,
    private val routineId: Long = 0L,
    private val repository: GymRepository,
    private val workoutRepository: WorkoutRepository? = null,
    private val routineRepository: RoutineRepository? = null
) : ViewModel() {

    private val _currentWorkoutId = MutableStateFlow(workoutId)
    private val _initError = MutableStateFlow<String?>(null)

    private val _elapsedSeconds = MutableStateFlow(0L)
    private val _isAddExerciseDialogOpen = MutableStateFlow(false)
    private val _isWorkoutFinished = MutableStateFlow(false)

    // Bandera de control para evitar guardados duplicados (debounce)
    private var isSaving = false
    private val _isSaving = MutableStateFlow(false)

    // Summary dialog state
    private val _isSummaryDialogOpen = MutableStateFlow(false)
    private val _workoutSummary = MutableStateFlow<WorkoutSummaryData?>(null)
    private val _cachedHistory = MutableStateFlow<List<WorkoutSessionWithDetails>>(emptyList())

    // Módulo Pro: Herramientas de entrenamiento en vivo
    private val _plateCalculatorExercise = MutableStateFlow<Exercise?>(null)
    private val _plateCalculatorInitialWeight = MutableStateFlow(60.0)
    private val _supersetPairs = MutableStateFlow<Map<Long, Long>>(emptyMap())
    private val _focusedExerciseId = MutableStateFlow<Long?>(null)
    private val _supersetLinkExercise = MutableStateFlow<Exercise?>(null)
    private val _supersetFeedbackMessage = MutableStateFlow<String?>(null)

    // Rest Timer state
    private val _isRestTimerActive = MutableStateFlow(false)
    private val _isRestTimerExpanded = MutableStateFlow(false)
    private val _restTimerTotalSeconds = MutableStateFlow(90)
    private val _restTimerRemainingSeconds = MutableStateFlow(90)
    private var restTimerJob: Job? = null

    private val _restTimerFinishedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val restTimerFinishedEvent: SharedFlow<Unit> = _restTimerFinishedEvent.asSharedFlow()

    private data class RestTimerData(
        val isActive: Boolean,
        val isExpanded: Boolean,
        val totalSeconds: Int,
        val remainingSeconds: Int
    )

    private val restTimerFlow = combine(
        _isRestTimerActive,
        _isRestTimerExpanded,
        _restTimerTotalSeconds,
        _restTimerRemainingSeconds
    ) { active, expanded, total, remaining ->
        RestTimerData(active, expanded, total, remaining)
    }

    private data class DialogState(
        val addDialog: Boolean,
        val finished: Boolean,
        val summaryDialog: Boolean,
        val summaryData: WorkoutSummaryData?,
        val initError: String?,
        val saving: Boolean
    )

    private data class ProToolsState(
        val plateExercise: Exercise?,
        val plateWeight: Double,
        val supersetPairs: Map<Long, Long>,
        val focusedExerciseId: Long?,
        val supersetLinkExercise: Exercise?,
        val feedbackMessage: String?
    )

    private val proToolsFlow = combine(
        _plateCalculatorExercise,
        _plateCalculatorInitialWeight,
        _supersetPairs,
        _focusedExerciseId,
        combine(_supersetLinkExercise, _supersetFeedbackMessage) { linkEx, msg -> Pair(linkEx, msg) }
    ) { plateEx, plateW, pairs, focusedId, (linkEx, msg) ->
        ProToolsState(plateEx, plateW, pairs, focusedId, linkEx, msg)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val workoutFlow: Flow<WorkoutDataState> = _currentWorkoutId.flatMapLatest { id ->
        if (id <= 0L) {
            flowOf(WorkoutDataState.Loading)
        } else {
            repository.getWorkoutWithSets(id).map { workoutWithSets ->
                if (workoutWithSets != null) {
                    WorkoutDataState.Success(workoutWithSets)
                } else {
                    WorkoutDataState.Loading
                }
            }
        }
    }

    val uiState: StateFlow<ActiveWorkoutUiState> = combine(
        workoutFlow,
        repository.getAllExercises(),
        _elapsedSeconds,
        restTimerFlow,
        combine(
            combine(
                _isAddExerciseDialogOpen,
                _isWorkoutFinished,
                _isSummaryDialogOpen,
                _workoutSummary,
                combine(_initError, _isSaving) { err, saving -> Pair(err, saving) }
            ) { addDialog, finished, summaryDialog, summaryData, (initErr, saving) ->
                DialogState(addDialog, finished, summaryDialog, summaryData, initErr, saving)
            },
            proToolsFlow
        ) { dialogState, proTools -> Pair(dialogState, proTools) }
    ) { dataState, exercises, elapsed, timerData, (dialogState, proTools) ->
        val augmentedWorkoutWithSets = (dataState as? WorkoutDataState.Success)?.let { successState ->
            val original = successState.workoutWithSets
            val augmentedExercises = original.exercises.map { group ->
                val partnerId = proTools.supersetPairs[group.exercise.id]
                val label = if (partnerId != null) {
                    val minId = minOf(group.exercise.id, partnerId)
                    val maxId = maxOf(group.exercise.id, partnerId)
                    val distinctPairs = proTools.supersetPairs.entries
                        .filter { it.key < it.value }
                        .sortedBy { it.key }
                    val pairIndex = distinctPairs.indexOfFirst { it.key == minId && it.value == maxId }.coerceAtLeast(0)
                    val letter = ('A' + pairIndex)
                    val subIndex = if (group.exercise.id == minId) "1" else "2"
                    "Superserie $letter$subIndex"
                } else {
                    null
                }
                group.copy(supersetLabel = label)
            }
            original.copy(exercises = augmentedExercises)
        }

        val finalDataState = when {
            dialogState.initError != null && dataState !is WorkoutDataState.Success -> WorkoutDataState.Error(dialogState.initError)
            augmentedWorkoutWithSets != null -> WorkoutDataState.Success(augmentedWorkoutWithSets)
            else -> dataState
        }

        ActiveWorkoutUiState(
            dataState = finalDataState,
            allAvailableExercises = exercises,
            elapsedSeconds = elapsed,
            isAddExerciseDialogOpen = dialogState.addDialog,
            isWorkoutFinished = dialogState.finished,
            isSaving = dialogState.saving,
            isRestTimerActive = timerData.isActive,
            isRestTimerExpanded = timerData.isExpanded,
            restTimerTotalSeconds = timerData.totalSeconds,
            restTimerRemainingSeconds = timerData.remainingSeconds,
            isSummaryDialogOpen = dialogState.summaryDialog,
            workoutSummary = dialogState.summaryData,
            plateCalculatorExercise = proTools.plateExercise,
            plateCalculatorInitialWeight = proTools.plateWeight,
            supersetPairs = proTools.supersetPairs,
            focusedExerciseId = proTools.focusedExerciseId,
            supersetLinkExercise = proTools.supersetLinkExercise,
            supersetFeedbackMessage = proTools.feedbackMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ActiveWorkoutUiState()
    )

    // Marcas de tiempo reales (System.currentTimeMillis() - startTimestamp)
    private var workoutStartTimestamp: Long = 0L
    private var restTimerStartTimestamp: Long = 0L
    private var activeRoutineId: Long = routineId

    private fun persistCurrentState(overrideWorkoutWithSets: WorkoutWithSets? = null) {
        if (_isWorkoutFinished.value || isSaving) return
        val currentId = _currentWorkoutId.value
        if (currentId <= 0L) return
        val currentData = overrideWorkoutWithSets ?: uiState.value.workoutWithSets
        val startTs = currentData?.workout?.startTimeMillis
            ?.takeIf { it > 0L }
            ?: workoutStartTimestamp.takeIf { it > 0L }
            ?: System.currentTimeMillis()
        val title = currentData?.workout?.title ?: "Entrenamiento"

        com.example.gymtracker.data.local.ActiveWorkoutSessionStore.saveState(
            workoutId = currentId,
            routineId = activeRoutineId,
            title = title,
            startTimestamp = startTs,
            restTimerActive = _isRestTimerActive.value,
            restTimerExpanded = _isRestTimerExpanded.value,
            restTimerStartTimestamp = restTimerStartTimestamp,
            restTimerTotalSeconds = _restTimerTotalSeconds.value,
            supersetPairs = _supersetPairs.value,
            focusedExerciseId = _focusedExerciseId.value,
            workoutWithSets = currentData
        )
    }

    init {
        // Inicialización segura del entrenamiento con recuperación automática de sesión en curso:
        viewModelScope.launch {
            try {
                val persisted = com.example.gymtracker.data.local.ActiveWorkoutSessionStore.loadState()

                if (workoutId > 0L) {
                    _currentWorkoutId.value = workoutId
                    if (persisted != null && persisted.workoutId == workoutId) {
                        activeRoutineId = persisted.routineId
                        workoutStartTimestamp = persisted.startTimestamp
                        _supersetPairs.value = persisted.supersetPairs
                        _focusedExerciseId.value = persisted.focusedExerciseId
                        restoreRestTimerIfNeeded(persisted)
                        reconcilePersistedSetsIfNeeded(workoutId, persisted.setsSnapshot)
                    }
                } else if (routineId > 0L) {
                    // Iniciar desde rutina específica:
                    activeRoutineId = routineId
                    val routineWithExercises = routineRepository?.getRoutineById(routineId)?.firstOrNull()
                    val title = routineWithExercises?.routine?.title ?: "Entrenamiento"
                    val now = System.currentTimeMillis()
                    workoutStartTimestamp = now
                    val workout = Workout(
                        title = title,
                        startTimeMillis = now
                    )
                    val newWorkoutId = repository.insertWorkout(workout)

                    routineWithExercises?.sortedRoutineExercises?.forEachIndexed { index, item ->
                        try {
                            val exercise = item.exercise
                            val preset = com.example.gymtracker.data.preset.DefaultRoutinesData.findExercisePreset(
                                routineTitle = title,
                                exerciseName = exercise.name
                            )
                            val setsCount = (preset?.targetSets ?: item.routineExercise.targetSets).coerceAtLeast(1)
                            val repRange = preset?.targetRepRange
                                ?: item.routineExercise.targetRepRange.ifBlank { "8-12 reps" }

                            for (setNum in 1..setsCount) {
                                val initialSet = WorkoutSet(
                                    workoutId = newWorkoutId,
                                    exerciseId = exercise.id,
                                    exerciseName = exercise.name,
                                    setNumber = setNum,
                                    weightKg = 0.0,
                                    reps = 0,
                                    isCompleted = false,
                                    exerciseOrder = index,
                                    targetRepRange = repRange
                                )
                                repository.insertSet(initialSet)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    _currentWorkoutId.value = newWorkoutId
                } else {
                    // Verificar si hay un entrenamiento activo sin finalizar en almacenamiento local antes de crear uno vacío
                    val existingActive = repository.getActiveWorkout().firstOrNull()
                    if (persisted != null && persisted.isActive && persisted.workoutId > 0L && existingActive?.id == persisted.workoutId) {
                        _currentWorkoutId.value = persisted.workoutId
                        activeRoutineId = persisted.routineId
                        workoutStartTimestamp = persisted.startTimestamp
                        _supersetPairs.value = persisted.supersetPairs
                        _focusedExerciseId.value = persisted.focusedExerciseId
                        restoreRestTimerIfNeeded(persisted)
                        reconcilePersistedSetsIfNeeded(persisted.workoutId, persisted.setsSnapshot)
                    } else {
                        // Iniciar entrenamiento vacío (routineId == 0L y workoutId == 0L):
                        val dateStr = SimpleDateFormat("EEEE, d MMM", Locale("es", "ES")).format(Date())
                        val capitalized = dateStr.replaceFirstChar { it.uppercase() }
                        val now = System.currentTimeMillis()
                        workoutStartTimestamp = now
                        val newWorkout = Workout(
                            title = "Entrenamiento de $capitalized",
                            startTimeMillis = now
                        )
                        val newWorkoutId = repository.insertWorkout(newWorkout)
                        _currentWorkoutId.value = newWorkoutId
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _initError.value = "Error al iniciar sesión: ${e.localizedMessage ?: "desconocido"}"
            }
        }

        // Sincronizar automáticamente en almacenamiento local (SharedPreferences JSON) cada vez que cambie el estado del entreno
        viewModelScope.launch {
            uiState.collect { state ->
                val workoutData = state.workoutWithSets
                if (workoutData != null && !state.isWorkoutFinished) {
                    if (workoutData.workout.startTimeMillis > 0L) {
                        workoutStartTimestamp = workoutData.workout.startTimeMillis
                    }
                    // Actualizar inmediatamente el cronómetro al recibir datos
                    if (!_isSummaryDialogOpen.value) {
                        val currentElapsed = (System.currentTimeMillis() - workoutData.workout.startTimeMillis) / 1000L
                        _elapsedSeconds.value = currentElapsed.coerceAtLeast(0L)
                    }
                    persistCurrentState(workoutData)
                }
            }
        }

        // Live stopwatch ticker basado en marcas de tiempo reales (System.currentTimeMillis() - startTimestamp)
        viewModelScope.launch {
            while (true) {
                delay(250L)
                val workout = uiState.value.workoutWithSets?.workout
                val startTs = workout?.startTimeMillis?.takeIf { it > 0L } ?: workoutStartTimestamp
                if (startTs > 0L && !_isSummaryDialogOpen.value && !_isWorkoutFinished.value) {
                    val currentElapsed = (System.currentTimeMillis() - startTs) / 1000L
                    _elapsedSeconds.value = if (currentElapsed > 0L) currentElapsed else 0L
                }
            }
        }

        // Cachear historial previo para detectar nuevos PRs de 1RM al finalizar entreno
        viewModelScope.launch {
            workoutRepository?.getWorkoutHistory()?.collect { historyList ->
                _cachedHistory.value = historyList
            }
        }
    }

    private suspend fun reconcilePersistedSetsIfNeeded(workoutId: Long, snapshotSets: List<WorkoutSet>) {
        if (snapshotSets.isEmpty()) return
        try {
            val currentRoomSets = repository.getSetsForWorkout(workoutId).firstOrNull().orEmpty()
            if (currentRoomSets.isEmpty()) {
                snapshotSets.forEach { set ->
                    repository.insertSet(set.copy(id = 0L, workoutId = workoutId))
                }
            } else {
                val roomMap = currentRoomSets.associateBy { it.id }
                snapshotSets.forEach { snap ->
                    val roomSet = roomMap[snap.id]
                    if (roomSet != null && (roomSet.weightKg != snap.weightKg || roomSet.reps != snap.reps || roomSet.isCompleted != snap.isCompleted)) {
                        repository.updateSet(
                            roomSet.copy(
                                weightKg = snap.weightKg,
                                reps = snap.reps,
                                isCompleted = snap.isCompleted
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun restoreRestTimerIfNeeded(persisted: com.example.gymtracker.data.local.PersistedActiveWorkoutState) {
        if (!persisted.restTimerActive || persisted.restTimerStartTimestamp <= 0L) return
        val elapsedSec = ((System.currentTimeMillis() - persisted.restTimerStartTimestamp) / 1000L).toInt().coerceAtLeast(0)
        val remaining = persisted.restTimerTotalSeconds - elapsedSec
        if (remaining > 0) {
            restTimerStartTimestamp = persisted.restTimerStartTimestamp
            _restTimerTotalSeconds.value = persisted.restTimerTotalSeconds
            _restTimerRemainingSeconds.value = remaining
            _isRestTimerActive.value = true
            _isRestTimerExpanded.value = persisted.restTimerExpanded
            launchRestTimerLoop()
        } else {
            _isRestTimerActive.value = false
            _isRestTimerExpanded.value = false
        }
    }

    // ==========================================
    // REST TIMER ACTIONS (Real-Time Timestamps)
    // ==========================================

    private fun launchRestTimerLoop() {
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            while (_isRestTimerActive.value) {
                val elapsedSec = ((System.currentTimeMillis() - restTimerStartTimestamp) / 1000L).toInt().coerceAtLeast(0)
                val remaining = (_restTimerTotalSeconds.value - elapsedSec).coerceAtLeast(0)
                _restTimerRemainingSeconds.value = remaining
                if (remaining <= 0) {
                    break
                }
                delay(200L)
            }
            if (_isRestTimerActive.value && _restTimerRemainingSeconds.value <= 0) {
                _restTimerRemainingSeconds.value = 0
                _restTimerFinishedEvent.tryEmit(Unit)
                delay(1500L)
                _isRestTimerActive.value = false
                _isRestTimerExpanded.value = false
                persistCurrentState()
            }
        }
    }

    fun startRestTimer(seconds: Int = 90, expandInitially: Boolean = false) {
        val targetSeconds = if (seconds > 0) seconds else 90
        restTimerStartTimestamp = System.currentTimeMillis()
        _restTimerTotalSeconds.value = targetSeconds
        _restTimerRemainingSeconds.value = targetSeconds
        _isRestTimerActive.value = true
        _isRestTimerExpanded.value = expandInitially
        persistCurrentState()
        launchRestTimerLoop()
    }

    fun openRestTimer(seconds: Int = 90) {
        startRestTimer(seconds, expandInitially = true)
    }

    fun closeRestTimer() {
        restTimerJob?.cancel()
        _isRestTimerActive.value = false
        _isRestTimerExpanded.value = false
        persistCurrentState()
    }

    fun adjustRestTimer(deltaSeconds: Int) {
        if (!_isRestTimerActive.value) return
        val elapsedSec = ((System.currentTimeMillis() - restTimerStartTimestamp) / 1000L).toInt().coerceAtLeast(0)
        val currentRemaining = (_restTimerTotalSeconds.value - elapsedSec).coerceAtLeast(0)
        val newRemaining = (currentRemaining + deltaSeconds).coerceAtLeast(0)
        val newTotal = maxOf(_restTimerTotalSeconds.value, newRemaining)

        _restTimerTotalSeconds.value = newTotal
        _restTimerRemainingSeconds.value = newRemaining
        val newElapsedSec = newTotal - newRemaining
        restTimerStartTimestamp = System.currentTimeMillis() - (newElapsedSec * 1000L)
        persistCurrentState()

        if (newRemaining == 0) {
            _restTimerFinishedEvent.tryEmit(Unit)
        }
    }

    fun addRestTimerSeconds(deltaSeconds: Int) = adjustRestTimer(deltaSeconds)

    fun setRestTimerExpanded(expanded: Boolean) {
        _isRestTimerExpanded.value = expanded
        persistCurrentState()
    }

    fun toggleRestTimerExpanded() {
        _isRestTimerExpanded.value = !_isRestTimerExpanded.value
        persistCurrentState()
    }

    // ==========================================
    // WORKOUT ACTIONS
    // ==========================================

    fun openAddExerciseDialog() {
        _isAddExerciseDialogOpen.value = true
    }

    fun closeAddExerciseDialog() {
        _isAddExerciseDialogOpen.value = false
    }

    fun addExerciseToWorkout(exercise: Exercise) {
        val currentId = _currentWorkoutId.value
        if (currentId <= 0L) return
        viewModelScope.launch {
            val currentGroups = uiState.value.workoutWithSets?.exercises.orEmpty()
            val nextOrder = (currentGroups.flatMap { it.sets }.maxOfOrNull { it.exerciseOrder } ?: -1) + 1
            val workoutTitle = uiState.value.workout?.title ?: ""
            val preset = com.example.gymtracker.data.preset.DefaultRoutinesData.findExercisePreset(workoutTitle, exercise.name)
            val setsCount = preset?.targetSets ?: 1
            val repRange = preset?.targetRepRange ?: "8-12 reps"
            for (setNum in 1..setsCount) {
                val newSet = WorkoutSet(
                    workoutId = currentId,
                    exerciseId = exercise.id,
                    exerciseName = exercise.name,
                    setNumber = setNum,
                    weightKg = 0.0,
                    reps = 0,
                    isCompleted = false,
                    exerciseOrder = nextOrder,
                    targetRepRange = repRange
                )
                repository.insertSet(newSet)
            }
            _isAddExerciseDialogOpen.value = false
        }
    }

    fun addSetToExercise(exerciseId: Long) {
        val currentId = _currentWorkoutId.value
        if (currentId <= 0L) return
        viewModelScope.launch {
            val currentGroup = uiState.value.workoutWithSets?.exercises?.find { it.exercise.id == exerciseId }
            val nextSetNumber = (currentGroup?.sets?.maxOfOrNull { it.setNumber } ?: 0) + 1
            val lastSet = currentGroup?.sets?.lastOrNull()
            val workoutTitle = uiState.value.workout?.title ?: ""
            val fallbackRange = com.example.gymtracker.data.preset.DefaultRoutinesData
                .findExercisePreset(workoutTitle, currentGroup?.exercise?.name ?: "")?.targetRepRange ?: "8-12 reps"
            val newSet = WorkoutSet(
                workoutId = currentId,
                exerciseId = exerciseId,
                setNumber = nextSetNumber,
                weightKg = lastSet?.weightKg ?: 0.0,
                reps = lastSet?.reps ?: 0,
                isCompleted = false,
                exerciseOrder = lastSet?.exerciseOrder ?: 0,
                targetRepRange = lastSet?.targetRepRange?.ifBlank { fallbackRange } ?: fallbackRange
            )
            repository.insertSet(newSet)
        }
    }

    fun updateSetWeight(set: WorkoutSet, weight: Double) {
        viewModelScope.launch {
            repository.updateSet(set.copy(weightKg = weight))
        }
    }

    fun updateSetReps(set: WorkoutSet, reps: Int) {
        viewModelScope.launch {
            repository.updateSet(set.copy(reps = reps))
        }
    }

    fun toggleSetCompleted(set: WorkoutSet, restSeconds: Int = 90) {
        viewModelScope.launch {
            val newStatus = !set.isCompleted
            repository.updateSet(set.copy(isCompleted = newStatus))
            if (newStatus) {
                val partnerId = _supersetPairs.value[set.exerciseId]
                if (partnerId != null) {
                    val currentGroups = uiState.value.workoutWithSets?.exercises ?: emptyList()
                    val currentGroup = currentGroups.find { it.exercise.id == set.exerciseId }
                    val partnerGroup = currentGroups.find { it.exercise.id == partnerId }

                    val currentCompleted = (currentGroup?.sets?.count { it.isCompleted } ?: 0) + 1
                    val partnerCompleted = partnerGroup?.sets?.count { it.isCompleted } ?: 0

                    if (currentCompleted > partnerCompleted) {
                        // El ejercicio par aún debe realizar su serie: enfocarlo y NO activar descanso
                        _focusedExerciseId.value = partnerId
                        _supersetFeedbackMessage.value = "⚡ Serie completada. Turno inmediato de ${partnerGroup?.exercise?.name ?: "ejercicio par"} (sin descanso)"
                    } else {
                        // Ambos completaron su serie en la superserie: descanso y desenfocar
                        _focusedExerciseId.value = null
                        _supersetFeedbackMessage.value = null
                        startRestTimer(restSeconds, expandInitially = false)
                    }
                } else {
                    startRestTimer(restSeconds, expandInitially = false)
                }
            }
        }
    }

    // ==========================================
    // HERRAMIENTAS PRO: CALCULADORA DE DISCOS Y CALENTAMIENTO
    // ==========================================

    fun openPlateCalculator(exercise: Exercise, initialWeight: Double = 60.0) {
        _plateCalculatorExercise.value = exercise
        _plateCalculatorInitialWeight.value = if (initialWeight > 0.0) initialWeight else 60.0
    }

    fun closePlateCalculator() {
        _plateCalculatorExercise.value = null
    }

    fun addWarmupSets(exerciseId: Long, warmupSets: List<Pair<Double, Int>>) {
        val currentId = _currentWorkoutId.value
        if (currentId <= 0L) return
        viewModelScope.launch {
            try {
                val currentGroup = uiState.value.workoutWithSets?.exercises?.find { it.exercise.id == exerciseId }
                val existingSets = currentGroup?.sets ?: emptyList()
                val existingNormal = existingSets.filter { it.setType != com.example.gymtracker.domain.model.SetType.WARMUP }

                warmupSets.forEachIndexed { idx, (weight, reps) ->
                    val warmupSet = WorkoutSet(
                        workoutId = currentId,
                        exerciseId = exerciseId,
                        exerciseName = currentGroup?.exercise?.name ?: "",
                        setNumber = idx + 1,
                        weightKg = weight,
                        reps = reps,
                        isCompleted = false,
                        setType = com.example.gymtracker.domain.model.SetType.WARMUP,
                        exerciseOrder = existingSets.firstOrNull()?.exerciseOrder ?: 0,
                        targetRepRange = existingSets.firstOrNull()?.targetRepRange ?: ""
                    )
                    repository.insertSet(warmupSet)
                }

                existingNormal.forEachIndexed { idx, normalSet ->
                    repository.updateSet(normalSet.copy(setNumber = warmupSets.size + idx + 1))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==========================================
    // HERRAMIENTAS PRO: NOTAS FIJAS POR EJERCICIO
    // ==========================================

    fun updatePinnedNote(exerciseId: Long, note: String) {
        viewModelScope.launch {
            try {
                repository.updateExercisePinnedNote(exerciseId, note)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==========================================
    // HERRAMIENTAS PRO: SUPERSERIES (BISERIES A1/A2)
    // ==========================================

    fun openSupersetLinkDialog(exercise: Exercise) {
        _supersetLinkExercise.value = exercise
    }

    fun closeSupersetLinkDialog() {
        _supersetLinkExercise.value = null
    }

    fun linkExercisesInSuperset(exerciseId1: Long, exerciseId2: Long) {
        if (exerciseId1 == exerciseId2) return
        val current = _supersetPairs.value.toMutableMap()
        val old1 = current[exerciseId1]
        if (old1 != null) current.remove(old1)
        val old2 = current[exerciseId2]
        if (old2 != null) current.remove(old2)

        current[exerciseId1] = exerciseId2
        current[exerciseId2] = exerciseId1
        _supersetPairs.value = current
        _supersetLinkExercise.value = null
        persistCurrentState()
    }

    fun unlinkSuperset(exerciseId: Long) {
        val current = _supersetPairs.value.toMutableMap()
        val partnerId = current.remove(exerciseId)
        if (partnerId != null) {
            current.remove(partnerId)
        }
        _supersetPairs.value = current
        persistCurrentState()
    }

    fun dismissSupersetFeedback() {
        _supersetFeedbackMessage.value = null
    }

    fun deleteSet(setId: Long) {
        viewModelScope.launch {
            repository.deleteSet(setId)
        }
    }

    fun deleteExercise(exerciseId: Long) {
        viewModelScope.launch {
            val currentGroup = uiState.value.workoutWithSets?.exercises?.find { it.exercise.id == exerciseId }
            currentGroup?.sets?.forEach { set ->
                repository.deleteSet(set.id)
            }
        }
    }

    fun updateWorkoutTitle(title: String) {
        viewModelScope.launch {
            val current = uiState.value.workoutWithSets?.workout ?: return@launch
            repository.updateWorkout(current.copy(title = title))
        }
    }

    // ==========================================
    // FLUJO POSTERIOR AL ENTRENAMIENTO
    // ==========================================

    fun onFinishWorkoutClicked() {
        if (isSaving) return
        val currentId = _currentWorkoutId.value
        if (currentId <= 0L) return
        closeRestTimer()
        val currentGrouped = uiState.value.workoutWithSets
        val allSets = currentGrouped?.exercises?.flatMap { it.sets } ?: emptyList()
        val completedSets = allSets.filter { it.isCompleted }

        // 1. Volumen total levantado en kg
        val totalVolumeKg = completedSets.sumOf { it.weightKg * it.reps }

        // 2. Duración total
        val totalDuration = _elapsedSeconds.value

        // 3. Cantidad de sets completados
        val completedSetsCount = completedSets.size
        val totalSetsCount = allSets.size

        val now = System.currentTimeMillis()
        val startTime = currentGrouped?.workout?.startTimeMillis ?: (now - totalDuration * 1000)

        val exercisesSummary = currentGrouped?.exercises?.map { group ->
            val compSets = group.sets.filter { it.isCompleted }
            ExerciseSummaryItem(
                exerciseId = group.exercise.id,
                exerciseName = group.exercise.name,
                muscleGroup = group.exercise.muscleGroup.displayName,
                completedSetsCount = compSets.size,
                totalSetsCount = group.sets.size,
                volumeKg = compSets.sumOf { it.weightKg * it.reps },
                setsDescription = group.sets.map { s ->
                    val status = if (s.isCompleted) "✓" else "—"
                    "Serie ${s.setNumber}: ${s.weightKg} kg × ${s.reps} reps [$status]"
                }
            )
        } ?: emptyList()

        // 4. Músculos trabajados hoy para el Mini Mapa Muscular
        val activeExerciseGroups = currentGrouped?.exercises?.filter { group ->
            group.sets.any { it.isCompleted }
        }.takeIf { !it.isNullOrEmpty() } ?: currentGrouped?.exercises ?: emptyList()

        val workedMuscles = RpgGamificationEngine.resolveWorkedMuscles(
            activeExerciseGroups.map { group ->
                Triple(
                    group.exercise.primaryMuscle,
                    group.exercise.muscleGroup.displayName,
                    group.exercise.name
                )
            }
        )

        // 5. Detectar nuevos Récords Personales (PRs) de 1RM en la sesión
        val currentExerciseSets = currentGrouped?.exercises?.map { group ->
            val compPairs = group.sets
                .filter { it.isCompleted }
                .map { it.weightKg to it.reps }
            Triple(group.exercise.id, group.exercise.name, compPairs)
        } ?: emptyList()

        val newPrs = RpgGamificationEngine.detectSessionPersonalRecords(
            currentWorkoutId = currentId,
            currentExerciseSets = currentExerciseSets,
            previousSessions = _cachedHistory.value
        )

        val xpEarned = RpgGamificationEngine.XP_PER_FINISHED_WORKOUT +
            (completedSetsCount * RpgGamificationEngine.XP_PER_COMPLETED_SET) +
            (newPrs.size * RpgGamificationEngine.XP_PER_PR_1RM)

        val summary = WorkoutSummaryData(
            workoutId = currentId,
            title = currentGrouped?.workout?.title?.ifBlank { "Entrenamiento" } ?: "Entrenamiento",
            startTime = startTime,
            endTime = now,
            durationSeconds = totalDuration,
            totalVolumeKg = totalVolumeKg,
            completedSetsCount = completedSetsCount,
            totalSetsCount = totalSetsCount,
            exercisesSummary = exercisesSummary,
            workedMuscles = workedMuscles,
            newPersonalRecords = newPrs,
            xpEarnedTotal = xpEarned
        )

        _workoutSummary.value = summary
        _isSummaryDialogOpen.value = true
    }

    fun dismissSummaryDialog() {
        if (isSaving) return
        _isSummaryDialogOpen.value = false
    }

    fun saveWorkoutFromSummary(notes: String = "", onFinished: () -> Unit) {
        // Bloqueo inmediato para impedir dobles toques (debounce)
        if (isSaving) return
        isSaving = true
        _isSaving.value = true

        val currentId = _currentWorkoutId.value
        if (currentId <= 0L) {
            isSaving = false
            _isSaving.value = false
            com.example.gymtracker.data.local.ActiveWorkoutSessionStore.clearState()
            onFinished()
            return
        }

        viewModelScope.launch {
            try {
                val summary = _workoutSummary.value
                val now = System.currentTimeMillis()
                val endTime = summary?.endTime ?: now
                val duration = summary?.durationSeconds ?: _elapsedSeconds.value
                val totalVolume = summary?.totalVolumeKg ?: 0.0

                // 1. Finalizar la sesión única existente (currentId) en WorkoutRepository (evita insertar sesión duplicada con id = 0)
                workoutRepository?.finishWorkoutSession(
                    sessionId = currentId,
                    endTime = endTime,
                    durationSeconds = duration,
                    totalVolumeKg = totalVolume
                )

                // 2. Sincronizar en GymRepository para compatibilidad de vistas
                repository.finishWorkout(currentId, notes)

                com.example.gymtracker.data.local.ActiveWorkoutSessionStore.clearState()
                _isSummaryDialogOpen.value = false
                _isWorkoutFinished.value = true
                onFinished()
            } catch (e: Exception) {
                e.printStackTrace()
                isSaving = false
                _isSaving.value = false
            }
        }
    }

    fun finishWorkout(notes: String = "") {
        // Bloqueo inmediato para impedir dobles toques (debounce)
        if (isSaving) return
        isSaving = true
        _isSaving.value = true

        val currentId = _currentWorkoutId.value
        if (currentId <= 0L) {
            isSaving = false
            _isSaving.value = false
            com.example.gymtracker.data.local.ActiveWorkoutSessionStore.clearState()
            return
        }

        viewModelScope.launch {
            try {
                closeRestTimer()
                repository.finishWorkout(currentId, notes)
                com.example.gymtracker.data.local.ActiveWorkoutSessionStore.clearState()
                _isWorkoutFinished.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                isSaving = false
                _isSaving.value = false
            }
        }
    }

    fun discardWorkout(onDiscarded: () -> Unit) {
        val currentId = _currentWorkoutId.value
        if (currentId <= 0L) {
            com.example.gymtracker.data.local.ActiveWorkoutSessionStore.clearState()
            onDiscarded()
            return
        }
        viewModelScope.launch {
            closeRestTimer()
            com.example.gymtracker.data.local.ActiveWorkoutSessionStore.clearState()
            repository.deleteWorkout(currentId)
            onDiscarded()
        }
    }

    class Factory(
        private val workoutId: Long = 0L,
        private val routineId: Long = 0L,
        private val repository: GymRepository,
        private val workoutRepository: WorkoutRepository? = null,
        private val routineRepository: RoutineRepository? = null
    ) : ViewModelProvider.Factory {

        constructor(
            workoutId: Long,
            repository: GymRepository,
            workoutRepository: WorkoutRepository? = null
        ) : this(
            workoutId = workoutId,
            routineId = 0L,
            repository = repository,
            workoutRepository = workoutRepository,
            routineRepository = null
        )

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ActiveWorkoutViewModel(
                workoutId = workoutId,
                routineId = routineId,
                repository = repository,
                workoutRepository = workoutRepository,
                routineRepository = routineRepository
            ) as T
        }
    }
}
