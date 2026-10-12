package com.example.gymtracker.ui.screens.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.domain.model.Exercise
import com.example.gymtracker.domain.model.MuscleGroup
import com.example.gymtracker.domain.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExercisesUiState(
    val exercises: List<Exercise> = emptyList(),
    val filteredExercises: List<Exercise> = emptyList(),
    val selectedMuscleGroup: MuscleGroup? = null,
    val searchQuery: String = "",
    val isCreateDialogOpen: Boolean = false
)

class ExercisesViewModel(
    private val repository: GymRepository
) : ViewModel() {

    private val _selectedMuscleGroup = MutableStateFlow<MuscleGroup?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isCreateDialogOpen = MutableStateFlow(false)

    val uiState: StateFlow<ExercisesUiState> = combine(
        repository.getAllExercises(),
        _selectedMuscleGroup,
        _searchQuery,
        _isCreateDialogOpen
    ) { exercises, muscleGroup, query, isDialogOpen ->
        val filtered = exercises.filter { ex ->
            val matchesGroup = muscleGroup == null || ex.muscleGroup == muscleGroup
            val matchesQuery = query.isBlank() ||
                ex.name.contains(query, ignoreCase = true) ||
                ex.muscleGroup.displayName.contains(query, ignoreCase = true) ||
                ex.equipment.contains(query, ignoreCase = true)
            matchesGroup && matchesQuery
        }

        ExercisesUiState(
            exercises = exercises,
            filteredExercises = filtered,
            selectedMuscleGroup = muscleGroup,
            searchQuery = query,
            isCreateDialogOpen = isDialogOpen
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExercisesUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSelectMuscleGroup(group: MuscleGroup?) {
        _selectedMuscleGroup.value = group
    }

    fun openCreateDialog() {
        _isCreateDialogOpen.value = true
    }

    fun closeCreateDialog() {
        _isCreateDialogOpen.value = false
    }

    fun createExercise(name: String, muscleGroup: MuscleGroup, equipment: String, restSeconds: Int) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val exercise = Exercise(
                name = name.trim(),
                muscleGroup = muscleGroup,
                equipment = equipment.ifBlank { "Barra" },
                defaultRestSeconds = if (restSeconds > 0) restSeconds else 90
            )
            repository.insertExercise(exercise)
            _isCreateDialogOpen.value = false
        }
    }

    class Factory(private val repository: GymRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExercisesViewModel(repository) as T
        }
    }
}
