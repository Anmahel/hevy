package com.example.gymtracker.ui.screens.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.data.local.entity.ExerciseEntity
import com.example.gymtracker.data.preset.ExerciseImageProvider
import com.example.gymtracker.domain.model.HypertrophyMuscle
import com.example.gymtracker.domain.repository.ExerciseRepository
import com.example.gymtracker.domain.repository.WeightRepository
import com.example.gymtracker.domain.repository.WorkoutRepository
import com.example.gymtracker.domain.util.RpgGamificationEngine
import com.example.gymtracker.domain.util.StrengthRankInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExerciseListUiState(
    val exercises: List<ExerciseEntity> = emptyList(),
    val filteredExercises: List<ExerciseEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedMuscleGroup: String? = null,
    val isSearchBarActive: Boolean = false,
    val isAddDialogOpen: Boolean = false,
    val editingExercise: ExerciseEntity? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Módulo 4: Rangos de Fuerza por Ejercicio (1RM vs Peso Corporal)
    val strengthRanksByExerciseId: Map<Long, StrengthRankInfo> = emptyMap(),
    val currentBodyweightKg: Double = 75.0
)

class ExerciseListViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository? = null,
    private val weightRepository: WeightRepository? = null
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedMuscleGroup = MutableStateFlow<String?>(null)
    private val _isSearchBarActive = MutableStateFlow(false)
    private val _isAddDialogOpen = MutableStateFlow(false)
    private val _editingExercise = MutableStateFlow<ExerciseEntity?>(null)

    private val historyFlow = workoutRepository?.getWorkoutHistory() ?: flowOf(emptyList())
    private val weightLogsFlow = weightRepository?.getAllWeightLogs() ?: flowOf(emptyList())

    private data class DialogState(
        val isSearchActive: Boolean,
        val isAddOpen: Boolean,
        val editingExercise: ExerciseEntity?
    )

    private val dialogStateFlow = combine(
        _isSearchBarActive,
        _isAddDialogOpen,
        _editingExercise
    ) { sActive, addOpen, editing ->
        DialogState(sActive, addOpen, editing)
    }

    val uiState: StateFlow<ExerciseListUiState> = combine(
        exerciseRepository.getAllExercises(),
        _searchQuery,
        _selectedMuscleGroup,
        dialogStateFlow,
        combine(historyFlow, weightLogsFlow) { history, weights -> Pair(history, weights) }
    ) { allExercises, query, muscleGroup, dialogState, (historyList, weightLogs) ->
        val filtered = allExercises.filter { exercise ->
            val matchesMuscle = if (muscleGroup == null) {
                true
            } else {
                val targetEnum = HypertrophyMuscle.fromSingleMuscleText(muscleGroup)
                val exEnum = HypertrophyMuscle.fromExerciseInfo(
                    primaryMuscle = exercise.primaryMuscle,
                    muscleGroup = exercise.muscleGroup,
                    exerciseName = exercise.name
                )
                exercise.muscleGroup.equals(muscleGroup, ignoreCase = true) ||
                    exercise.primaryMuscle.equals(muscleGroup, ignoreCase = true) ||
                    (targetEnum != null && exEnum == targetEnum)
            }

            val matchesQuery = query.isBlank() ||
                exercise.name.contains(query, ignoreCase = true) ||
                exercise.muscleGroup.contains(query, ignoreCase = true) ||
                exercise.primaryMuscle.contains(query, ignoreCase = true) ||
                exercise.secondaryMuscles.contains(query, ignoreCase = true) ||
                exercise.equipment.contains(query, ignoreCase = true)

            matchesMuscle && matchesQuery
        }

        val currentBw = weightLogs.firstOrNull()?.weightKg?.toDouble() ?: 75.0
        val ranksMap = RpgGamificationEngine.calculateExerciseStrengthRanks(
            exercises = allExercises,
            sessions = historyList,
            bodyweightKg = currentBw
        )

        ExerciseListUiState(
            exercises = allExercises,
            filteredExercises = filtered,
            searchQuery = query,
            selectedMuscleGroup = muscleGroup,
            isSearchBarActive = dialogState.isSearchActive,
            isAddDialogOpen = dialogState.isAddOpen,
            editingExercise = dialogState.editingExercise,
            isLoading = false,
            strengthRanksByExerciseId = ranksMap,
            currentBodyweightKg = currentBw
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExerciseListUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onMuscleGroupSelected(muscleGroup: String?) {
        _selectedMuscleGroup.value = if (_selectedMuscleGroup.value == muscleGroup) null else muscleGroup
    }

    fun onSearchBarActiveChanged(active: Boolean) {
        _isSearchBarActive.value = active
        if (!active) {
            _searchQuery.value = ""
        }
    }

    fun openAddDialog() {
        _editingExercise.value = null
        _isAddDialogOpen.value = true
    }

    fun openEditDialog(exercise: ExerciseEntity) {
        _editingExercise.value = exercise
        _isAddDialogOpen.value = true
    }

    fun closeAddDialog() {
        _isAddDialogOpen.value = false
        _editingExercise.value = null
    }

    /**
     * Crea un nuevo ejercicio personalizado o actualiza cualquier ejercicio existente
     * (incluidos los 43 ejercicios por defecto) conservando su ID, historial y récords de 1RM.
     */
    fun saveOrUpdateExercise(
        name: String,
        primaryMuscle: String,
        secondaryMuscles: String = "",
        equipment: String,
        mediaUrlOrPath: String = "",
        defaultRestSeconds: Int = 90
    ) {
        if (name.isBlank()) return
        val editing = _editingExercise.value
        viewModelScope.launch {
            val cleanName = name.trim()
            val cleanPrimary = primaryMuscle.trim()
            val cleanSecondary = secondaryMuscles.trim()
            val cleanEquipment = equipment.trim()
            val validRest = if (defaultRestSeconds > 0) defaultRestSeconds else 90

            val resolvedMedia = mediaUrlOrPath.trim().ifBlank {
                editing?.gifPathOrUrl?.ifBlank { editing.thumbnailUrl }
                    ?: ExerciseImageProvider.getImageUrlForExercise(cleanName, cleanPrimary)
            }

            if (editing != null) {
                val updated = editing.copy(
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
            } else {
                val customExercise = ExerciseEntity(
                    name = cleanName,
                    muscleGroup = cleanPrimary,
                    primaryMuscle = cleanPrimary,
                    secondaryMuscles = cleanSecondary,
                    equipment = cleanEquipment,
                    isCustom = true,
                    defaultRestSeconds = validRest,
                    gifPathOrUrl = resolvedMedia,
                    thumbnailUrl = resolvedMedia
                )
                exerciseRepository.insertCustomExercise(customExercise)
            }

            _isAddDialogOpen.value = false
            _editingExercise.value = null
        }
    }

    fun createCustomExercise(
        name: String,
        muscleGroup: String,
        equipment: String,
        defaultRestSeconds: Int = 90
    ) {
        saveOrUpdateExercise(
            name = name,
            primaryMuscle = muscleGroup,
            secondaryMuscles = "",
            equipment = equipment,
            mediaUrlOrPath = "",
            defaultRestSeconds = defaultRestSeconds
        )
    }

    fun deleteExercise(exercise: ExerciseEntity) {
        viewModelScope.launch {
            exerciseRepository.deleteExercise(exercise)
        }
    }

    class Factory(
        private val repository: ExerciseRepository,
        private val workoutRepository: WorkoutRepository? = null,
        private val weightRepository: WeightRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExerciseListViewModel(
                exerciseRepository = repository,
                workoutRepository = workoutRepository,
                weightRepository = weightRepository
            ) as T
        }
    }
}
