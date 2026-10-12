package com.example.gymtracker.ui.screens.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.data.local.entity.ExerciseEntity
import com.example.gymtracker.data.local.entity.RoutineEntity
import com.example.gymtracker.domain.repository.ExerciseRepository
import com.example.gymtracker.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditRoutineUiState(
    val routineId: Long = 0L,
    val title: String = "",
    val notes: String = "",
    val selectedExercises: List<ExerciseEntity> = emptyList(),
    val allAvailableExercises: List<ExerciseEntity> = emptyList(),
    val isExercisePickerOpen: Boolean = false,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false
)

class EditRoutineViewModel(
    private val routineId: Long,
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditRoutineUiState(routineId = routineId, isLoading = true))
    val uiState: StateFlow<EditRoutineUiState> = _uiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EditRoutineUiState(routineId = routineId, isLoading = true)
    )

    init {
        viewModelScope.launch {
            // Cargar todos los ejercicios disponibles para el selector
            val allExercises = exerciseRepository.getAllExercises().firstOrNull() ?: emptyList()
            _uiState.update { it.copy(allAvailableExercises = allExercises) }

            // Si es edición de una rutina existente, cargar sus datos
            if (routineId > 0L) {
                val existingRoutine = routineRepository.getRoutineById(routineId).firstOrNull()
                if (existingRoutine != null) {
                    _uiState.update {
                        it.copy(
                            title = existingRoutine.routine.title,
                            notes = existingRoutine.routine.notes,
                            selectedExercises = existingRoutine.sortedExercises,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } else {
                _uiState.update {
                    it.copy(
                        title = "Nueva Rutina",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onTitleChanged(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onNotesChanged(newNotes: String) {
        _uiState.update { it.copy(notes = newNotes) }
    }

    fun openExercisePicker() {
        _uiState.update { it.copy(isExercisePickerOpen = true) }
    }

    fun closeExercisePicker() {
        _uiState.update { it.copy(isExercisePickerOpen = false) }
    }

    fun addExercise(exercise: ExerciseEntity) {
        _uiState.update { state ->
            state.copy(
                selectedExercises = state.selectedExercises + exercise,
                isExercisePickerOpen = false
            )
        }
    }

    fun removeExercise(index: Int) {
        _uiState.update { state ->
            val updated = state.selectedExercises.toMutableList()
            if (index in updated.indices) {
                updated.removeAt(index)
            }
            state.copy(selectedExercises = updated)
        }
    }

    fun moveExerciseUp(index: Int) {
        if (index <= 0) return
        _uiState.update { state ->
            val updated = state.selectedExercises.toMutableList()
            val item = updated.removeAt(index)
            updated.add(index - 1, item)
            state.copy(selectedExercises = updated)
        }
    }

    fun moveExerciseDown(index: Int) {
        _uiState.update { state ->
            val updated = state.selectedExercises.toMutableList()
            if (index < updated.size - 1) {
                val item = updated.removeAt(index)
                updated.add(index + 1, item)
            }
            state.copy(selectedExercises = updated)
        }
    }

    fun saveRoutine() {
        val current = _uiState.value
        val routineTitle = current.title.trim().ifEmpty { "Rutina sin nombre" }
        val exerciseIds = current.selectedExercises.map { it.id }

        viewModelScope.launch {
            if (routineId > 0L) {
                // Actualización
                val updatedRoutine = RoutineEntity(
                    id = routineId,
                    title = routineTitle,
                    notes = current.notes.trim()
                )
                routineRepository.updateRoutineWithExercises(updatedRoutine, exerciseIds)
            } else {
                // Creación
                val newRoutine = RoutineEntity(
                    title = routineTitle,
                    notes = current.notes.trim()
                )
                routineRepository.createRoutine(newRoutine, exerciseIds)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    class Factory(
        private val routineId: Long,
        private val routineRepository: RoutineRepository,
        private val exerciseRepository: ExerciseRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditRoutineViewModel(routineId, routineRepository, exerciseRepository) as T
        }
    }
}
