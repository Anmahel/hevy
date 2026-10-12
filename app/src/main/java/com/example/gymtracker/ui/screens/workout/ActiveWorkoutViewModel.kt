package com.example.gymtracker.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.data.local.entity.WorkoutSessionEntity
import com.example.gymtracker.data.local.entity.WorkoutSetEntity
import com.example.gymtracker.domain.model.Exercise
import com.example.gymtracker.domain.model.Workout
import com.example.gymtracker.domain.model.WorkoutSet
import com.example.gymtracker.domain.model.WorkoutWithSets
import com.example.gymtracker.domain.repository.GymRepository
import com.example.gymtracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    val exercisesSummary: List<ExerciseSummaryItem>
)

data class ActiveWorkoutUiState(
    val workoutWithSets: WorkoutWithSets? = null,
    val allAvailableExercises: List<Exercise> = emptyList(),
    val elapsedSeconds: Long = 0,
    val isAddExerciseDialogOpen: Boolean = false,
    val isWorkoutFinished: Boolean = false,
    // Rest Timer state
    val isRestTimerActive: Boolean = false,
    val isRestTimerExpanded: Boolean = false,
    val restTimerTotalSeconds: Int = 90,
    val restTimerRemainingSeconds: Int = 90,
    // Summary Dialog state
    val isSummaryDialogOpen: Boolean = false,
    val workoutSummary: WorkoutSummaryData? = null
) {
    // Backward compatibility getters
    val isTimerOpen: Boolean get() = isRestTimerActive
    val timerSeconds: Int get() = restTimerRemainingSeconds
}

class ActiveWorkoutViewModel(
    private val workoutId: Long,
    private val repository: GymRepository,
    private val workoutRepository: WorkoutRepository? = null
) : ViewModel() {

    private val _elapsedSeconds = MutableStateFlow(0L)
    private val _isAddExerciseDialogOpen = MutableStateFlow(false)
    private val _isWorkoutFinished = MutableStateFlow(false)

    // Summary dialog state
    private val _isSummaryDialogOpen = MutableStateFlow(false)
    private val _workoutSummary = MutableStateFlow<WorkoutSummaryData?>(null)

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
        val summaryData: WorkoutSummaryData?
    )

    val uiState: StateFlow<ActiveWorkoutUiState> = combine(
        repository.getWorkoutWithSets(workoutId),
        repository.getAllExercises(),
        _elapsedSeconds,
        restTimerFlow,
        combine(
            _isAddExerciseDialogOpen,
            _isWorkoutFinished,
            _isSummaryDialogOpen,
            _workoutSummary
        ) { addDialog, finished, summaryDialog, summaryData ->
            DialogState(addDialog, finished, summaryDialog, summaryData)
        }
    ) { workoutWithSets, exercises, elapsed, timerData, dialogState ->
        ActiveWorkoutUiState(
            workoutWithSets = workoutWithSets,
            allAvailableExercises = exercises,
            elapsedSeconds = elapsed,
            isAddExerciseDialogOpen = dialogState.addDialog,
            isWorkoutFinished = dialogState.finished,
            isRestTimerActive = timerData.isActive,
            isRestTimerExpanded = timerData.isExpanded,
            restTimerTotalSeconds = timerData.totalSeconds,
            restTimerRemainingSeconds = timerData.remainingSeconds,
            isSummaryDialogOpen = dialogState.summaryDialog,
            workoutSummary = dialogState.summaryData
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ActiveWorkoutUiState()
    )

    init {
        // Live stopwatch ticker
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                val workout = uiState.value.workoutWithSets?.workout
                if (workout != null && !_isSummaryDialogOpen.value && !_isWorkoutFinished.value) {
                    val currentElapsed = (System.currentTimeMillis() - workout.startTimeMillis) / 1000
                    _elapsedSeconds.value = if (currentElapsed > 0) currentElapsed else 0
                }
            }
        }
    }

    // ==========================================
    // REST TIMER ACTIONS
    // ==========================================

    fun startRestTimer(seconds: Int = 90, expandInitially: Boolean = false) {
        val targetSeconds = if (seconds > 0) seconds else 90
        _restTimerTotalSeconds.value = targetSeconds
        _restTimerRemainingSeconds.value = targetSeconds
        _isRestTimerActive.value = true
        _isRestTimerExpanded.value = expandInitially

        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            while (_restTimerRemainingSeconds.value > 0) {
                delay(1000L)
                _restTimerRemainingSeconds.value = _restTimerRemainingSeconds.value - 1
            }
            _restTimerFinishedEvent.tryEmit(Unit)
            delay(1500L)
            _isRestTimerActive.value = false
            _isRestTimerExpanded.value = false
        }
    }

    fun openRestTimer(seconds: Int = 90) {
        startRestTimer(seconds, expandInitially = true)
    }

    fun closeRestTimer() {
        restTimerJob?.cancel()
        _isRestTimerActive.value = false
        _isRestTimerExpanded.value = false
    }

    fun adjustRestTimer(deltaSeconds: Int) {
        if (!_isRestTimerActive.value) return
        val current = _restTimerRemainingSeconds.value
        val newRemaining = (current + deltaSeconds).coerceAtLeast(0)
        _restTimerRemainingSeconds.value = newRemaining
        if (newRemaining > _restTimerTotalSeconds.value) {
            _restTimerTotalSeconds.value = newRemaining
        }
        if (newRemaining == 0) {
            _restTimerFinishedEvent.tryEmit(Unit)
        }
    }

    fun addRestTimerSeconds(deltaSeconds: Int) = adjustRestTimer(deltaSeconds)

    fun setRestTimerExpanded(expanded: Boolean) {
        _isRestTimerExpanded.value = expanded
    }

    fun toggleRestTimerExpanded() {
        _isRestTimerExpanded.value = !_isRestTimerExpanded.value
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
        viewModelScope.launch {
            val newSet = WorkoutSet(
                workoutId = workoutId,
                exerciseId = exercise.id,
                exerciseName = exercise.name,
                setNumber = 1,
                weightKg = 0.0,
                reps = 10,
                isCompleted = false
            )
            repository.insertSet(newSet)
            _isAddExerciseDialogOpen.value = false
        }
    }

    fun addSetToExercise(exerciseId: Long) {
        viewModelScope.launch {
            val currentGroup = uiState.value.workoutWithSets?.exercises?.find { it.exercise.id == exerciseId }
            val nextSetNumber = (currentGroup?.sets?.maxOfOrNull { it.setNumber } ?: 0) + 1
            val lastSet = currentGroup?.sets?.lastOrNull()
            val newSet = WorkoutSet(
                workoutId = workoutId,
                exerciseId = exerciseId,
                setNumber = nextSetNumber,
                weightKg = lastSet?.weightKg ?: 0.0,
                reps = lastSet?.reps ?: 10,
                isCompleted = false
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
                startRestTimer(restSeconds, expandInitially = false)
            }
        }
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

    /**
     * Calcula métricas clave al pulsar "Finalizar":
     * 1. Volumen total levantado en kg (suma de weightKg * reps de sets completados).
     * 2. Duración total en segundos.
     * 3. Cantidad de sets completados.
     * Abre el diálogo de felicitaciones [WorkoutSummaryDialog].
     */
    fun onFinishWorkoutClicked() {
        closeRestTimer()
        val currentGrouped = uiState.value.workoutWithSets
        val allSets = currentGrouped?.exercises?.flatMap { it.sets } ?: emptyList()
        val completedSets = allSets.filter { it.isCompleted }

        // 1. Volumen total levantado en kg (suma de weightKg * reps de sets completados)
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

        val summary = WorkoutSummaryData(
            workoutId = workoutId,
            title = currentGrouped?.workout?.title?.ifBlank { "Entrenamiento" } ?: "Entrenamiento",
            startTime = startTime,
            endTime = now,
            durationSeconds = totalDuration,
            totalVolumeKg = totalVolumeKg,
            completedSetsCount = completedSetsCount,
            totalSetsCount = totalSetsCount,
            exercisesSummary = exercisesSummary
        )

        _workoutSummary.value = summary
        _isSummaryDialogOpen.value = true
    }

    fun dismissSummaryDialog() {
        _isSummaryDialogOpen.value = false
    }

    /**
     * Guarda la sesión de entrenamiento en Room mediante [WorkoutRepository]
     * y finaliza la sesión en [GymRepository], cerrando el diálogo y notificando a la UI.
     */
    fun saveWorkoutFromSummary(notes: String = "", onFinished: () -> Unit) {
        viewModelScope.launch {
            val summary = _workoutSummary.value ?: run {
                // Fallback directo si no hubiera resumen en memoria
                finishWorkout(notes)
                onFinished()
                return@launch
            }

            val currentGrouped = uiState.value.workoutWithSets

            // 1. Guardar en Room mediante WorkoutRepository si está disponible
            if (workoutRepository != null) {
                val sessionEntity = WorkoutSessionEntity(
                    id = 0,
                    routineId = null,
                    title = summary.title,
                    startTime = summary.startTime,
                    endTime = summary.endTime,
                    durationSeconds = summary.durationSeconds,
                    totalVolumeKg = summary.totalVolumeKg
                )

                val setEntities = currentGrouped?.exercises?.flatMap { group ->
                    group.sets.map { set ->
                        WorkoutSetEntity(
                            id = 0,
                            workoutSessionId = 0, // Se asocia atómicamente en WorkoutDao.saveWorkoutSessionWithSets
                            exerciseId = group.exercise.id,
                            setNumber = set.setNumber,
                            setType = "Normal",
                            weightKg = set.weightKg,
                            reps = set.reps,
                            isCompleted = set.isCompleted
                        )
                    }
                } ?: emptyList()

                workoutRepository.saveWorkoutSession(sessionEntity, setEntities)
            }

            // 2. Sincronizar en GymRepository para compatibilidad de vistas
            repository.finishWorkout(workoutId, notes)

            _isSummaryDialogOpen.value = false
            _isWorkoutFinished.value = true
            onFinished()
        }
    }

    fun finishWorkout(notes: String = "") {
        viewModelScope.launch {
            closeRestTimer()
            repository.finishWorkout(workoutId, notes)
            _isWorkoutFinished.value = true
        }
    }

    fun discardWorkout(onDiscarded: () -> Unit) {
        viewModelScope.launch {
            closeRestTimer()
            repository.deleteWorkout(workoutId)
            onDiscarded()
        }
    }

    class Factory(
        private val workoutId: Long,
        private val repository: GymRepository,
        private val workoutRepository: WorkoutRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ActiveWorkoutViewModel(workoutId, repository, workoutRepository) as T
        }
    }
}
