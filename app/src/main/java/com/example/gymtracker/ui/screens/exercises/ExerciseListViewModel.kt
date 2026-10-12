package com.example.gymtracker.ui.screens.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.data.local.entity.ExerciseEntity
import com.example.gymtracker.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExerciseListUiState(
    val exercises: List<ExerciseEntity> = emptyList(),
    val filteredExercises: List<ExerciseEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedMuscleGroup: String? = null,
    val isSearchBarActive: Boolean = false,
    val isAddDialogOpen: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ExerciseListViewModel(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedMuscleGroup = MutableStateFlow<String?>(null)
    private val _isSearchBarActive = MutableStateFlow(false)
    private val _isAddDialogOpen = MutableStateFlow(false)

    val uiState: StateFlow<ExerciseListUiState> = combine(
        exerciseRepository.getAllExercises(),
        _searchQuery,
        _selectedMuscleGroup,
        _isSearchBarActive,
        _isAddDialogOpen
    ) { allExercises, query, muscleGroup, isSearchActive, isAddOpen ->
        val filtered = allExercises.filter { exercise ->
            val matchesMuscle = muscleGroup == null ||
                exercise.muscleGroup.equals(muscleGroup, ignoreCase = true)

            val matchesQuery = query.isBlank() ||
                exercise.name.contains(query, ignoreCase = true) ||
                exercise.muscleGroup.contains(query, ignoreCase = true) ||
                exercise.equipment.contains(query, ignoreCase = true)

            matchesMuscle && matchesQuery
        }

        ExerciseListUiState(
            exercises = allExercises,
            filteredExercises = filtered,
            searchQuery = query,
            selectedMuscleGroup = muscleGroup,
            isSearchBarActive = isSearchActive,
            isAddDialogOpen = isAddOpen,
            isLoading = false
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
        // Toggle selection or clear if already selected
        _selectedMuscleGroup.value = if (_selectedMuscleGroup.value == muscleGroup) null else muscleGroup
    }

    fun onSearchBarActiveChanged(active: Boolean) {
        _isSearchBarActive.value = active
        if (!active) {
            _searchQuery.value = ""
        }
    }

    fun openAddDialog() {
        _isAddDialogOpen.value = true
    }

    fun closeAddDialog() {
        _isAddDialogOpen.value = false
    }

    fun createCustomExercise(
        name: String,
        muscleGroup: String,
        equipment: String,
        defaultRestSeconds: Int = 90
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val customExercise = ExerciseEntity(
                name = name.trim(),
                muscleGroup = muscleGroup.trim(),
                equipment = equipment.trim(),
                isCustom = true,
                defaultRestSeconds = if (defaultRestSeconds > 0) defaultRestSeconds else 90,
                primaryMuscle = muscleGroup.trim()
            )
            exerciseRepository.insertCustomExercise(customExercise)
            _isAddDialogOpen.value = false
        }
    }

    fun deleteExercise(exercise: ExerciseEntity) {
        viewModelScope.launch {
            exerciseRepository.deleteExercise(exercise)
        }
    }

    class Factory(
        private val repository: ExerciseRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExerciseListViewModel(repository) as T
        }
    }
}
