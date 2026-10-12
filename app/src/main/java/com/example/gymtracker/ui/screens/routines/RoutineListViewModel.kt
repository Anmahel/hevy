package com.example.gymtracker.ui.screens.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.data.local.relation.RoutineWithOrderedExercises
import com.example.gymtracker.domain.model.Workout
import com.example.gymtracker.domain.model.WorkoutSet
import com.example.gymtracker.domain.repository.GymRepository
import com.example.gymtracker.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RoutineListUiState(
    val routines: List<RoutineWithOrderedExercises> = emptyList(),
    val isLoading: Boolean = false
)

class RoutineListViewModel(
    private val routineRepository: RoutineRepository,
    private val gymRepository: GymRepository
) : ViewModel() {

    val uiState: StateFlow<RoutineListUiState> = routineRepository
        .getAllRoutinesWithOrderedExercises()
        .map { routinesList ->
            RoutineListUiState(
                routines = routinesList,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RoutineListUiState(isLoading = true)
        )

    fun duplicateRoutine(routineId: Long) {
        viewModelScope.launch {
            routineRepository.duplicateRoutine(routineId)
        }
    }

    fun deleteRoutine(routineId: Long) {
        viewModelScope.launch {
            routineRepository.deleteRoutine(routineId)
        }
    }

    /**
     * Carga o regenera las 6 rutinas sugeridas día a día.
     */
    fun seedDefaultRoutines(forceReload: Boolean = false) {
        viewModelScope.launch {
            routineRepository.seedDefaultRoutines(forceReload)
        }
    }

    /**
     * Inicia una sesión activa de entrenamiento precargando los ejercicios de esta rutina.
     */
    fun startWorkoutFromRoutine(
        routineWithExercises: RoutineWithOrderedExercises,
        onWorkoutStarted: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val workout = Workout(
                title = routineWithExercises.routine.title,
                startTimeMillis = System.currentTimeMillis()
            )
            val newWorkoutId = gymRepository.insertWorkout(workout)

            // Insert initial default sets for each exercise in the routine
            routineWithExercises.sortedExercises.forEach { exercise ->
                val initialSet = WorkoutSet(
                    workoutId = newWorkoutId,
                    exerciseId = exercise.id,
                    exerciseName = exercise.name,
                    setNumber = 1,
                    weightKg = 0.0,
                    reps = 10,
                    isCompleted = false
                )
                gymRepository.insertSet(initialSet)
            }

            onWorkoutStarted(newWorkoutId)
        }
    }

    class Factory(
        private val routineRepository: RoutineRepository,
        private val gymRepository: GymRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RoutineListViewModel(routineRepository, gymRepository) as T
        }
    }
}
