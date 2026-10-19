package com.example.gymtracker.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.data.local.entity.CustomMealBlockEntity
import com.example.gymtracker.data.local.entity.DailyNutritionLogEntity
import com.example.gymtracker.data.local.entity.ExerciseEntity
import com.example.gymtracker.data.local.entity.RoutineExerciseEntity
import com.example.gymtracker.data.local.entity.WeightLogEntity
import com.example.gymtracker.data.local.relation.RoutineWithOrderedExercises
import com.example.gymtracker.data.local.relation.WorkoutSessionWithDetails
import com.example.gymtracker.domain.repository.ExerciseRepository
import com.example.gymtracker.domain.repository.GymRepository
import com.example.gymtracker.domain.repository.RoutineRepository
import com.example.gymtracker.domain.repository.WeightRepository
import com.example.gymtracker.domain.repository.WorkoutRepository
import com.example.gymtracker.domain.util.AthleteXpProfile
import com.example.gymtracker.domain.util.BackupJsonManager
import com.example.gymtracker.domain.util.BackupValidationResult
import com.example.gymtracker.domain.util.LocalStorageStats
import com.example.gymtracker.domain.util.RpgGamificationEngine
import com.example.gymtracker.domain.util.StrengthRankInfo
import com.example.gymtracker.ui.screens.workout.ExerciseSummaryItem
import com.example.gymtracker.ui.screens.workout.WorkoutSummaryData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HistoryUiState(
    val sessions: List<WorkoutSessionWithDetails> = emptyList(),
    val expandedSessionIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    // Módulo 4: Gamificación RPG y Rangos de Fuerza
    val athleteXpProfile: AthleteXpProfile = AthleteXpProfile(),
    val strengthRanks: List<StrengthRankInfo> = emptyList(),
    val currentBodyweightKg: Double = 75.0,
    val selectedShareCardSummary: WorkoutSummaryData? = null,
    // Módulo 5: Estadísticas de Almacenamiento Local y Backup & Restore (.JSON)
    val storageStats: LocalStorageStats = LocalStorageStats(),
    val isBackupDialogOpen: Boolean = false
)

class HistoryViewModel(
    private val workoutRepository: WorkoutRepository,
    private val gymRepository: GymRepository? = null,
    private val weightRepository: WeightRepository? = null,
    private val exerciseRepository: ExerciseRepository? = null,
    private val routineRepository: RoutineRepository? = null
) : ViewModel() {

    private val _expandedSessionIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _selectedShareCardSummary = MutableStateFlow<WorkoutSummaryData?>(null)
    private val _isBackupDialogOpen = MutableStateFlow(false)

    // Caches en memoria para exportación instantánea en 1 clic
    private var cachedRoutines: List<RoutineWithOrderedExercises> = emptyList()
    private var cachedSessions: List<WorkoutSessionWithDetails> = emptyList()
    private var cachedExercises: List<ExerciseEntity> = emptyList()
    private var cachedWeightLogs: List<WeightLogEntity> = emptyList()
    private var cachedDailyNutritionLogs: List<DailyNutritionLogEntity> = emptyList()
    private var cachedCustomMealBlocks: List<CustomMealBlockEntity> = emptyList()

    private val todayDateKey: String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private val weightLogsFlow = weightRepository?.getAllWeightLogs() ?: flowOf(emptyList())
    private val todayNutritionFlow = weightRepository?.observeDailyNutritionLog(todayDateKey) ?: flowOf(null)
    private val allNutritionLogsFlow = weightRepository?.observeAllDailyNutritionLogs() ?: flowOf(emptyList())
    private val customMealsFlow = weightRepository?.observeCustomMealBlocks() ?: flowOf(emptyList())
    private val exercisesFlow = exerciseRepository?.getAllExercises() ?: flowOf(emptyList())
    private val routinesFlow = routineRepository?.getAllRoutinesWithOrderedExercises() ?: flowOf(emptyList())

    private data class ExtraBackupData(
        val routines: List<RoutineWithOrderedExercises>,
        val allNutrition: List<DailyNutritionLogEntity>,
        val customMeals: List<CustomMealBlockEntity>,
        val isBackupOpen: Boolean
    )

    private val extraBackupFlow = combine(
        routinesFlow,
        allNutritionLogsFlow,
        customMealsFlow,
        _isBackupDialogOpen
    ) { routines, allNutr, meals, backupOpen ->
        ExtraBackupData(routines, allNutr, meals, backupOpen)
    }

    val uiState: StateFlow<HistoryUiState> = combine(
        workoutRepository.getWorkoutHistory(),
        _expandedSessionIds,
        combine(weightLogsFlow, todayNutritionFlow, exercisesFlow) { wLogs, nutrLog, exList ->
            Triple(wLogs, nutrLog, exList)
        },
        _selectedShareCardSummary,
        extraBackupFlow
    ) { historyList, expandedIds, (weightLogs, todayNutrition, exercisesList), shareCard, extra ->
        cachedRoutines = extra.routines
        cachedSessions = historyList
        cachedExercises = exercisesList
        cachedWeightLogs = weightLogs
        cachedDailyNutritionLogs = extra.allNutrition
        cachedCustomMealBlocks = extra.customMeals

        val hasLoggedNutritionToday = todayNutrition != null && (
            todayNutrition.consumedCalories > 0 ||
                todayNutrition.creatineTaken ||
                todayNutrition.waterMl > 0
            )

        val xpProfile = RpgGamificationEngine.calculateAthleteXpProfile(
            sessions = historyList,
            weightLogsCount = weightLogs.size,
            hasLoggedNutritionToday = hasLoggedNutritionToday
        )

        val currentBw = weightLogs.firstOrNull()?.weightKg?.toDouble() ?: 75.0
        val ranksMap = RpgGamificationEngine.calculateExerciseStrengthRanks(
            exercises = exercisesList,
            sessions = historyList,
            bodyweightKg = currentBw
        )

        // Ordenar rangos mostrando primero ejercicios con 1RM registrado, luego los principales
        val sortedRanks = ranksMap.values.sortedWith(
            compareByDescending<StrengthRankInfo> { it.bestOneRepMaxKg > 0.0 }
                .thenByDescending { it.bestOneRepMaxKg }
                .thenBy { it.exerciseName }
        )

        val storageStats = BackupJsonManager.calculateStorageStats(
            routines = extra.routines,
            sessions = historyList,
            exercises = exercisesList,
            weightLogs = weightLogs,
            dailyNutritionLogs = extra.allNutrition,
            customMealBlocks = extra.customMeals
        )

        HistoryUiState(
            sessions = historyList,
            expandedSessionIds = expandedIds,
            isLoading = false,
            athleteXpProfile = xpProfile,
            strengthRanks = sortedRanks,
            currentBodyweightKg = currentBw,
            selectedShareCardSummary = shareCard,
            storageStats = storageStats,
            isBackupDialogOpen = extra.isBackupOpen
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(isLoading = true)
    )

    fun openBackupDialog() {
        _isBackupDialogOpen.value = true
    }

    fun closeBackupDialog() {
        _isBackupDialogOpen.value = false
    }

    /**
     * Genera el JSON completo de copia de seguridad con todo el estado de la aplicación.
     */
    fun generateBackupJson(): String {
        return BackupJsonManager.exportFullBackupToJson(
            routines = cachedRoutines,
            sessions = cachedSessions,
            exercises = cachedExercises,
            weightLogs = cachedWeightLogs,
            dailyNutritionLogs = cachedDailyNutritionLogs,
            customMealBlocks = cachedCustomMealBlocks
        )
    }

    /**
     * Valida la estructura del JSON y restaura todas las rutinas, entrenos, ejercicios, pesos y nutrición.
     */
    fun restoreBackupFromJson(
        rawJson: String,
        onResult: (Boolean, String) -> Unit
    ) {
        when (val validation = BackupJsonManager.validateAndParseBackupJson(rawJson)) {
            is BackupValidationResult.Invalid -> {
                onResult(false, validation.errorMessage)
            }
            is BackupValidationResult.Valid -> {
                val payload = validation.payload
                viewModelScope.launch {
                    try {
                        // 1. Restaurar ejercicios (actualizando si ya existen para evitar cascadas FK o insertando si son nuevos)
                        payload.exercises.forEach { ex ->
                            val existing = exerciseRepository?.getExerciseById(ex.id)
                            if (existing != null) {
                                if (ex.isCustom || ex.pinnedNote.isNotBlank()) {
                                    exerciseRepository.updateExercise(ex)
                                }
                            } else {
                                exerciseRepository?.insertCustomExercise(ex)
                            }
                        }

                        // 2. Restaurar rutinas junto con sus ejercicios ordenados
                        payload.routines.forEach { routineRecord ->
                            val rId = routineRepository?.insertRoutine(routineRecord.routine) ?: routineRecord.routine.id
                            val crossRefs = routineRecord.exerciseIds.mapIndexed { idx, exId ->
                                RoutineExerciseEntity(
                                    routineId = rId,
                                    exerciseId = exId,
                                    orderIndex = idx
                                )
                            }
                            if (crossRefs.isNotEmpty()) {
                                routineRepository?.insertRoutineExercises(crossRefs)
                            }
                        }

                        // 3. Restaurar historial de sesiones de entrenamiento y sus series
                        payload.workoutSessions.forEach { sessionRecord ->
                            workoutRepository.saveWorkoutSession(
                                session = sessionRecord.session,
                                sets = sessionRecord.sets
                            )
                        }

                        // 4. Restaurar historial de pesos corporales y medidas antropométricas
                        payload.weightLogs.forEach { weightLog ->
                            weightRepository?.insertOrUpdateWeightLog(weightLog)
                        }

                        // 5. Restaurar registros diarios de nutrición 1-Tap, creatina e hidratación
                        payload.dailyNutritionLogs.forEach { nutrLog ->
                            weightRepository?.upsertDailyNutritionLog(nutrLog)
                        }

                        // 6. Restaurar bloques personalizados de comidas rápidas
                        payload.customMealBlocks.forEach { mealBlock ->
                            weightRepository?.insertCustomMealBlock(mealBlock)
                        }

                        onResult(true, "Restauración completada al instante. ${validation.summaryMessage}")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onResult(false, "Error al restaurar en base de datos: ${e.localizedMessage ?: "desconocido"}")
                    }
                }
            }
        }
    }

    fun toggleSessionExpanded(sessionId: Long) {
        val current = _expandedSessionIds.value
        _expandedSessionIds.value = if (current.contains(sessionId)) {
            current - sessionId
        } else {
            current + sessionId
        }
    }

    fun openShareCardForSession(sessionWithDetails: WorkoutSessionWithDetails) {
        val allSessions = uiState.value.sessions
        val session = sessionWithDetails.session
        val grouped = sessionWithDetails.setsGroupedByExercise

        val exercisesSummary = grouped.map { (exercise, sets) ->
            val compSets = sets.filter { it.isCompleted }
            ExerciseSummaryItem(
                exerciseId = exercise.id,
                exerciseName = exercise.name,
                muscleGroup = exercise.muscleGroup,
                completedSetsCount = compSets.size,
                totalSetsCount = sets.size,
                volumeKg = compSets.sumOf { it.weightKg * it.reps },
                setsDescription = sets.map { s ->
                    val status = if (s.isCompleted) "✓" else "—"
                    "Serie ${s.setNumber}: ${s.weightKg} kg × ${s.reps} reps [$status]"
                }
            )
        }

        val workedMuscles = RpgGamificationEngine.resolveWorkedMuscles(
            grouped.keys.map { ex ->
                Triple(ex.primaryMuscle, ex.muscleGroup, ex.name)
            }
        )

        val earlierSessions = allSessions.filter {
            it.session.id != session.id && it.session.startTime < session.startTime
        }
        val currentExerciseSets = grouped.map { (ex, sets) ->
            val compPairs = sets.filter { it.isCompleted }.map { it.weightKg to it.reps }
            Triple(ex.id, ex.name, compPairs)
        }
        val sessionPrs = RpgGamificationEngine.detectSessionPersonalRecords(
            currentWorkoutId = session.id,
            currentExerciseSets = currentExerciseSets,
            previousSessions = earlierSessions
        )

        val completedSetsCount = sessionWithDetails.sets.count { it.set.isCompleted }
        val xpEarned = RpgGamificationEngine.XP_PER_FINISHED_WORKOUT +
            (completedSetsCount * RpgGamificationEngine.XP_PER_COMPLETED_SET) +
            (sessionPrs.size * RpgGamificationEngine.XP_PER_PR_1RM)

        _selectedShareCardSummary.value = WorkoutSummaryData(
            workoutId = session.id,
            title = session.title,
            startTime = session.startTime,
            endTime = session.endTime ?: session.startTime,
            durationSeconds = session.durationSeconds,
            totalVolumeKg = session.totalVolumeKg,
            completedSetsCount = completedSetsCount,
            totalSetsCount = sessionWithDetails.sets.size,
            exercisesSummary = exercisesSummary,
            workedMuscles = workedMuscles,
            newPersonalRecords = sessionPrs,
            xpEarnedTotal = xpEarned
        )
    }

    fun dismissShareCard() {
        _selectedShareCardSummary.value = null
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            workoutRepository.deleteWorkoutSession(sessionId)
            gymRepository?.deleteWorkout(sessionId)
        }
    }

    class Factory(
        private val workoutRepository: WorkoutRepository,
        private val gymRepository: GymRepository? = null,
        private val weightRepository: WeightRepository? = null,
        private val exerciseRepository: ExerciseRepository? = null,
        private val routineRepository: RoutineRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(
                workoutRepository = workoutRepository,
                gymRepository = gymRepository,
                weightRepository = weightRepository,
                exerciseRepository = exerciseRepository,
                routineRepository = routineRepository
            ) as T
        }
    }
}
