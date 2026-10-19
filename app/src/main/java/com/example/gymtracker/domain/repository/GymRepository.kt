package com.example.gymtracker.domain.repository

import com.example.gymtracker.domain.model.Exercise
import com.example.gymtracker.domain.model.Workout
import com.example.gymtracker.domain.model.WorkoutSet
import com.example.gymtracker.domain.model.WorkoutWithSets
import kotlinx.coroutines.flow.Flow

interface GymRepository {
    // Exercises
    fun getAllExercises(): Flow<List<Exercise>>
    suspend fun getExerciseById(id: Long): Exercise?
    suspend fun insertExercise(exercise: Exercise): Long
    suspend fun updateExercisePinnedNote(exerciseId: Long, note: String)

    // Workouts
    fun getAllWorkouts(): Flow<List<Workout>>
    fun getActiveWorkout(): Flow<Workout?>
    fun getWorkoutWithSets(workoutId: Long): Flow<WorkoutWithSets?>
    suspend fun insertWorkout(workout: Workout): Long
    suspend fun updateWorkout(workout: Workout)
    suspend fun finishWorkout(workoutId: Long, notes: String = "")
    suspend fun deleteWorkout(workoutId: Long)

    // Sets
    fun getSetsForWorkout(workoutId: Long): Flow<List<WorkoutSet>>
    suspend fun insertSet(set: WorkoutSet): Long
    suspend fun updateSet(set: WorkoutSet)
    suspend fun deleteSet(setId: Long)
    suspend fun getPreviousPerformance(exerciseId: Long, setNumber: Int, currentWorkoutId: Long): String

    // Prepopulate
    suspend fun prepopulateDefaultExercises()
}
