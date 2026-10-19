package com.example.gymtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.gymtracker.data.local.entity.WorkoutEntity
import com.example.gymtracker.data.local.entity.WorkoutSessionEntity
import com.example.gymtracker.data.local.entity.WorkoutSetEntity
import com.example.gymtracker.data.local.relation.CompletedMuscleSetRecord
import com.example.gymtracker.data.local.relation.WorkoutSessionWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // --- NUEVO MODELO RELACIONAL HEVY (WorkoutSessionEntity + WorkoutSetEntity) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSession(session: WorkoutSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSets(sets: List<WorkoutSetEntity>)

    /**
     * Guarda una sesión completa de entrenamiento junto con todas sus series
     * dentro de una transacción atómica de SQLite (@Transaction).
     * Si cualquier paso falla, se revierte todo (rollback).
     */
    @Transaction
    suspend fun saveWorkoutSessionWithSets(
        session: WorkoutSessionEntity,
        sets: List<WorkoutSetEntity>
    ): Long {
        val sessionId = insertWorkoutSession(session)
        val linkedSets = sets.map { it.copy(workoutSessionId = sessionId) }
        insertWorkoutSets(linkedSets)
        return sessionId
    }

    /**
     * Obtiene el historial completo de entrenamientos con todos sus sets y detalles de ejercicios,
     * ordenado por fecha de inicio descendente (del más reciente al más antiguo).
     */
    @Transaction
    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun getWorkoutHistory(): Flow<List<WorkoutSessionWithDetails>>

    /**
     * Obtiene una sesión específica con sus series y detalles de ejercicio.
     */
    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId LIMIT 1")
    fun getWorkoutSessionWithDetails(sessionId: Long): Flow<WorkoutSessionWithDetails?>

    @Query("SELECT * FROM workout_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    fun getActiveSession(): Flow<WorkoutSessionEntity?>

    @Update
    suspend fun updateWorkoutSession(session: WorkoutSessionEntity)

    @Query("UPDATE workout_sessions SET endTime = :endTime, durationSeconds = :durationSeconds, totalVolumeKg = :totalVolumeKg WHERE id = :sessionId")
    suspend fun finishWorkoutSession(sessionId: Long, endTime: Long, durationSeconds: Long, totalVolumeKg: Double)

    @Query("SELECT * FROM workout_sessions WHERE id = :id LIMIT 1")
    suspend fun getWorkoutSessionById(id: Long): WorkoutSessionEntity?

    @Query("""
        SELECT ws.weightKg || ' kg × ' || ws.reps 
        FROM workout_sets ws 
        WHERE ws.exerciseId = :exerciseId AND ws.setNumber = :setNumber AND ws.isCompleted = 1 AND ws.workoutSessionId != :currentSessionId 
        ORDER BY ws.id DESC LIMIT 1
    """)
    suspend fun getPreviousSetPerformance(exerciseId: Long, setNumber: Int, currentSessionId: Long): String?

    @Query("""
        SELECT ws.weightKg || ' kg × ' || ws.reps 
        FROM workout_sets ws 
        WHERE ws.exerciseId = :exerciseId AND ws.isCompleted = 1 AND ws.workoutSessionId != :currentSessionId 
        ORDER BY ws.id DESC LIMIT 1
    """)
    suspend fun getLastExercisePerformance(exerciseId: Long, currentSessionId: Long): String?

    /**
     * Obtiene el ID de la última sesión finalizada en la que participó este ejercicio (distinta de la actual).
     */
    @Query("""
        SELECT ws.workoutSessionId 
        FROM workout_sets ws
        INNER JOIN workout_sessions s ON ws.workoutSessionId = s.id
        WHERE ws.exerciseId = :exerciseId AND ws.workoutSessionId != :currentSessionId AND s.endTime IS NOT NULL
        ORDER BY s.endTime DESC LIMIT 1
    """)
    suspend fun getLastCompletedSessionIdForExercise(exerciseId: Long, currentSessionId: Long): Long?

    /**
     * Obtiene todas las series de un ejercicio en una sesión específica.
     */
    @Query("""
        SELECT * FROM workout_sets 
        WHERE workoutSessionId = :sessionId AND exerciseId = :exerciseId 
        ORDER BY setNumber ASC
    """)
    suspend fun getSetsForExerciseInSession(sessionId: Long, exerciseId: Long): List<WorkoutSetEntity>

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteWorkoutSessionById(id: Long)

    /**
     * Consulta todas las sesiones completadas ordenadas por fecha de inicio para racha y almanaque.
     */
    @Query("SELECT * FROM workout_sessions WHERE endTime IS NOT NULL ORDER BY startTime ASC")
    fun getAllCompletedSessions(): Flow<List<WorkoutSessionEntity>>

    /**
     * Consulta las sesiones completadas con sus detalles en un rango de fechas.
     */
    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE startTime >= :startMillis AND startTime <= :endMillis AND endTime IS NOT NULL ORDER BY startTime ASC")
    fun getCompletedSessionsInRange(startMillis: Long, endMillis: Long): Flow<List<WorkoutSessionWithDetails>>

    /**
     * Cuenta el total de entrenamientos completados en un rango de fechas.
     */
    @Query("SELECT COUNT(*) FROM workout_sessions WHERE startTime >= :startMillis AND startTime <= :endMillis AND endTime IS NOT NULL")
    fun countSessionsInRange(startMillis: Long, endMillis: Long): Flow<Int>

    /**
     * Consulta todas las series completadas de sesiones finalizadas con los datos del ejercicio
     * y las marcas de tiempo de la sesión, para calcular volumen de hipertrofia y estado de recuperación.
     */
    @Query("""
        SELECT 
            ws.id AS setId,
            ws.exerciseId AS exerciseId,
            e.name AS exerciseName,
            e.muscleGroup AS muscleGroup,
            e.primaryMuscle AS primaryMuscle,
            e.secondaryMuscles AS secondaryMuscles,
            ws.weightKg AS weightKg,
            ws.reps AS reps,
            ws.setType AS setType,
            ws.isCompleted AS isCompleted,
            s.startTime AS sessionStartTime,
            s.endTime AS sessionEndTime
        FROM workout_sets ws
        INNER JOIN exercises e ON ws.exerciseId = e.id
        INNER JOIN workout_sessions s ON ws.workoutSessionId = s.id
        WHERE ws.isCompleted = 1 AND s.endTime IS NOT NULL
        ORDER BY s.startTime DESC
    """)
    fun getAllCompletedMuscleSets(): Flow<List<CompletedMuscleSetRecord>>


    // --- COMPATIBILIDAD CON VISTAS EXISTENTES ---

    @Query("SELECT * FROM workouts ORDER BY startTimeMillis DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE endTimeMillis IS NULL ORDER BY startTimeMillis DESC LIMIT 1")
    fun getActiveWorkout(): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workouts WHERE id = :id LIMIT 1")
    fun getWorkoutByIdFlow(id: Long): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workouts WHERE id = :id LIMIT 1")
    suspend fun getWorkoutById(id: Long): WorkoutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Query("UPDATE workouts SET endTimeMillis = :endTime, notes = :notes WHERE id = :workoutId")
    suspend fun finishWorkout(workoutId: Long, endTime: Long, notes: String)

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteWorkoutById(id: Long)
}
