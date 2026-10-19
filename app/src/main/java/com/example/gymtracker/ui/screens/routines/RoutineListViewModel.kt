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
            try {
                val routineTitle = routineWithExercises.routine.title
                val startTimestamp = System.currentTimeMillis()
                val workout = Workout(
                    title = routineTitle,
                    startTimeMillis = startTimestamp
                )
                val newWorkoutId = gymRepository.insertWorkout(workout)

                // Insertar la cantidad exacta de series (3 o 2 según corresponda) en orden estricto exerciseOrder ASC
                routineWithExercises.sortedRoutineExercises.forEachIndexed { index, item ->
                    try {
                        val exercise = item.exercise
                        val preset = com.example.gymtracker.data.preset.DefaultRoutinesData.findExercisePreset(
                            routineTitle = routineTitle,
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
                            gymRepository.insertSet(initialSet)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Persistir inmediatamente el entrenamiento activo para protección contra cierre en segundo plano
                com.example.gymtracker.data.local.ActiveWorkoutSessionStore.saveState(
                    workoutId = newWorkoutId,
                    routineId = routineWithExercises.routine.id,
                    title = routineTitle,
                    startTimestamp = startTimestamp,
                    restTimerActive = false,
                    restTimerExpanded = false,
                    restTimerStartTimestamp = 0L,
                    restTimerTotalSeconds = 90,
                    supersetPairs = emptyMap(),
                    focusedExerciseId = null,
                    workoutWithSets = null
                )

                onWorkoutStarted(newWorkoutId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
