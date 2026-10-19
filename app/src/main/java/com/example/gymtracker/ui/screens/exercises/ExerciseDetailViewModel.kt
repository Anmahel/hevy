package com.example.gymtracker.ui.screens.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.data.local.entity.ExerciseEntity
import com.example.gymtracker.data.local.entity.WorkoutSetEntity
import com.example.gymtracker.data.preset.ExerciseImageProvider
import com.example.gymtracker.domain.repository.ExerciseRepository
import com.example.gymtracker.domain.repository.WeightRepository
import com.example.gymtracker.domain.repository.WorkoutRepository
import com.example.gymtracker.domain.util.RpgGamificationEngine
import com.example.gymtracker.domain.util.StrengthRankInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExerciseHistoryEntry(
    val sessionId: Long,
    val sessionTitle: String,
    val formattedDate: String,
    val sets: List<WorkoutSetEntity>,
    val maxWeight: Double,
    val totalVolume: Double
)

data class ExerciseDetailUiState(
    val isLoading: Boolean = true,
    val exercise: ExerciseEntity? = null,
    val isEditDialogOpen: Boolean = false,
    val selectedTab: Int = 0, // 0 = Resumen, 1 = Historial, 2 = Instrucciones
    val maxWeightKg: Double = 0.0,
    val bestOneRepMaxKg: Double = 0.0,
    val maxVolumeKg: Double = 0.0,
    val totalCompletedSetsCount: Int = 0,
    val historySessions: List<ExerciseHistoryEntry> = emptyList(),
    val instructionsList: List<String> = emptyList(),
    // Módulo 4: Rango de Fuerza Competitivo (Bronce..Titán)
    val strengthRankInfo: StrengthRankInfo? = null,
    val currentBodyweightKg: Double = 75.0
)

class ExerciseDetailViewModel(
    private val exerciseId: Long,
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
    private val weightRepository: WeightRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseDetailUiState())
    val uiState: StateFlow<ExerciseDetailUiState> = _uiState.asStateFlow()

    init {
        loadExerciseAndStats()
    }

    private fun loadExerciseAndStats() {
        viewModelScope.launch {
            val weightLogsFlow = weightRepository?.getAllWeightLogs() ?: flowOf(emptyList())
            combine(
                exerciseRepository.getExerciseFlowById(exerciseId),
                workoutRepository.getWorkoutHistory(),
                weightLogsFlow
            ) { exerciseEntity, history, weightLogs ->
                if (exerciseEntity == null) {
                    _uiState.update { it.copy(isLoading = false, exercise = null) }
                    return@combine
                }

                // Filter sessions that contained this exercise with completed sets
                val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                val historyEntries = mutableListOf<ExerciseHistoryEntry>()
                var globalMaxWeight = 0.0
                var globalBest1Rm = 0.0
                var globalMaxVolume = 0.0
                var totalCompletedSets = 0

                history.forEach { sessionDetails ->
                    val exerciseSets = sessionDetails.sets
                        .filter { it.set.exerciseId == exerciseId && it.set.isCompleted }
                        .map { it.set }
                        .sortedBy { it.setNumber }

                    if (exerciseSets.isNotEmpty()) {
                        totalCompletedSets += exerciseSets.size
                        var sessionVolume = 0.0
                        var sessionMaxWeight = 0.0

                        exerciseSets.forEach { set ->
                            if (set.weightKg > sessionMaxWeight) sessionMaxWeight = set.weightKg
                            if (set.weightKg > globalMaxWeight) globalMaxWeight = set.weightKg

                            val est1Rm = RpgGamificationEngine.calculateOneRepMax(set.weightKg, set.reps)
                            if (est1Rm > globalBest1Rm) globalBest1Rm = est1Rm

                            sessionVolume += (set.weightKg * set.reps)
                        }

                        if (sessionVolume > globalMaxVolume) globalMaxVolume = sessionVolume

                        val dateStr = dateFormat.format(Date(sessionDetails.session.startTime))
                        historyEntries.add(
                            ExerciseHistoryEntry(
                                sessionId = sessionDetails.session.id,
                                sessionTitle = sessionDetails.session.title,
                                formattedDate = dateStr,
                                sets = exerciseSets,
                                maxWeight = sessionMaxWeight,
                                totalVolume = sessionVolume
                            )
                        )
                    }
                }

                val currentBw = weightLogs.firstOrNull()?.weightKg?.toDouble() ?: 75.0
                val rankInfo = RpgGamificationEngine.calculateStrengthRank(
                    exerciseId = exerciseEntity.id,
                    exerciseName = exerciseEntity.name,
                    muscleGroup = exerciseEntity.primaryMuscle.ifBlank { exerciseEntity.muscleGroup },
                    bestOneRepMaxKg = globalBest1Rm,
                    bodyweightKg = currentBw
                )

                val instructions = if (exerciseEntity.instructions.isNotBlank()) {
                    val raw = exerciseEntity.instructions
                    val splitLines = if (raw.contains("\n")) {
                        raw.split("\n")
                    } else {
                        raw.split(". ")
                    }
                    splitLines.map { it.trim() }.filter { it.isNotBlank() }
                } else {
                    getDefaultInstructionsFor(exerciseEntity.name, exerciseEntity.muscleGroup)
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        exercise = exerciseEntity,
                        maxWeightKg = globalMaxWeight,
                        bestOneRepMaxKg = globalBest1Rm,
                        maxVolumeKg = globalMaxVolume,
                        totalCompletedSetsCount = totalCompletedSets,
                        historySessions = historyEntries,
                        instructionsList = instructions,
                        strengthRankInfo = rankInfo,
                        currentBodyweightKg = currentBw
                    )
                }
            }.collect {}
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun openEditDialog() {
        _uiState.update { it.copy(isEditDialogOpen = true) }
    }

    fun closeEditDialog() {
        _uiState.update { it.copy(isEditDialogOpen = false) }
    }

    fun updateExerciseDetails(
        name: String,
        primaryMuscle: String,
        secondaryMuscles: String,
        equipment: String,
        mediaUrlOrPath: String,
        defaultRestSeconds: Int
    ) {
        val current = _uiState.value.exercise ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            val cleanName = name.trim()
            val cleanPrimary = primaryMuscle.trim()
            val cleanSecondary = secondaryMuscles.trim()
            val cleanEquipment = equipment.trim()
            val validRest = if (defaultRestSeconds > 0) defaultRestSeconds else 90
            val resolvedMedia = mediaUrlOrPath.trim().ifBlank {
                current.gifPathOrUrl.ifBlank { current.thumbnailUrl }
                    .ifBlank { ExerciseImageProvider.getImageUrlForExercise(cleanName, cleanPrimary) }
            }

            val updated = current.copy(
                name = cleanName,
                muscleGroup = cleanPrimary,
                primaryMuscle = cleanPrimary,
                secondaryMuscles = cleanSecondary,
                equipment = cleanEquipment,
                defaultRestSeconds = validRest,
                gifPathOrUrl = resolvedMedia,
                thumbnailUrl = resolvedMedia
            )
            exerciseRepository.updateExercise(updated)
            _uiState.update { it.copy(isEditDialogOpen = false) }
        }
    }

    fun updatePinnedNote(note: String) {
        viewModelScope.launch {
            try {
                exerciseRepository.updatePinnedNote(exerciseId, note)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getDefaultInstructionsFor(name: String, muscleGroup: String): List<String> {
        return listOf(
            "1. Preparación y postura: Ajusta el equipo o agarra la carga manteniendo la columna vertebral neutra y los pies firmemente apoyados.",
            "2. Fase excéntrica (bajada): Inicia el movimiento de manera controlada (2–3 segundos), manteniendo la tensión constante sobre el grupo muscular ($muscleGroup).",
            "3. Punto de estiramiento: Haz una breve pausa de 0.5 a 1 segundo en la posición más profunda sin perder la retracción escapular ni arquear la zona lumbar.",
            "4. Fase concéntrica (subida): Empuja o tira con fuerza explosiva controlada exhalando el aire en el punto de máxima contracción.",
            "5. Respiración y ritmo: Inhala en la bajada, exhala en el esfuerzo máximo. Evita bloquear rígidamente las articulaciones para proteger los tendones."
        )
    }

    class Factory(
        private val exerciseId: Long,
        private val exerciseRepository: ExerciseRepository,
        private val workoutRepository: WorkoutRepository,
        private val weightRepository: WeightRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExerciseDetailViewModel(
                exerciseId = exerciseId,
                exerciseRepository = exerciseRepository,
                workoutRepository = workoutRepository,
                weightRepository = weightRepository
            ) as T
        }
    }
}
