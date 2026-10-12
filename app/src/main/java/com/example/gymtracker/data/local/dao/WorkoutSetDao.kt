package com.example.gymtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gymtracker.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSetDao {
    @Query("SELECT * FROM workout_sets WHERE workoutSessionId = :workoutSessionId ORDER BY exerciseId ASC, setNumber ASC")
    fun getSetsForWorkout(workoutSessionId: Long): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM workout_sets WHERE exerciseId = :exerciseId AND isCompleted = 1")
    fun getCompletedSetsForExercise(exerciseId: Long): Flow<List<WorkoutSetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: WorkoutSetEntity): Long

    @Update
    suspend fun updateSet(set: WorkoutSetEntity)

    @Query("DELETE FROM workout_sets WHERE id = :id")
    suspend fun deleteSetById(id: Long)

    @Query("DELETE FROM workout_sets WHERE workoutSessionId = :workoutSessionId")
    suspend fun deleteSetsForWorkout(workoutSessionId: Long)
}
