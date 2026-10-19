package com.example.gymtracker.domain.repository

import com.example.gymtracker.data.local.entity.WorkoutSessionEntity
import com.example.gymtracker.data.local.entity.WorkoutSetEntity
import com.example.gymtracker.data.local.relation.WorkoutSessionWithDetails
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    /**
     * Emite el historial de entrenamientos completo con todos sus sets y detalles de ejercicios,
     * ordenado del más reciente al más antiguo.
     */
    fun getWorkoutHistory(): Flow<List<WorkoutSessionWithDetails>>

    /**
     * Emite el detalle completo de una sesión de entrenamiento específica.
     */
    fun getWorkoutSessionById(sessionId: Long): Flow<WorkoutSessionWithDetails?>

    /**
     * Emite la sesión actualmente activa (sin fecha de finalización), si existe.
     */
    fun getActiveSession(): Flow<WorkoutSessionEntity?>

    /**
     * Guarda una sesión completa junto con sus series dentro de una transacción atómica (@Transaction).
     */
    suspend fun saveWorkoutSession(
        session: WorkoutSessionEntity,
        sets: List<WorkoutSetEntity>
    ): Long

    /**
     * Actualiza los datos de una sesión en progreso.
     */
    suspend fun updateWorkoutSession(session: WorkoutSessionEntity)

    /**
     * Finaliza una sesión registrando su hora de fin, duración y volumen acumulado.
     */
    suspend fun finishWorkoutSession(
        sessionId: Long,
        endTime: Long,
        durationSeconds: Long,
        totalVolumeKg: Double
    )

    /**
     * Elimina una sesión y sus sets en cascada.
     */
    suspend fun deleteWorkoutSession(sessionId: Long)

    /**
     * Consulta todas las sesiones completadas para el cálculo de rachas.
     */
    fun getAllCompletedSessions(): Flow<List<WorkoutSessionEntity>>

    /**
     * Consulta sesiones completadas con detalles dentro de un rango temporal.
     */
    fun getCompletedSessionsInRange(startMillis: Long, endMillis: Long): Flow<List<WorkoutSessionWithDetails>>

    /**
     * Cuenta sesiones en un rango temporal.
     */
    fun countSessionsInRange(startMillis: Long, endMillis: Long): Flow<Int>

    /**
     * Consulta todas las series completadas de sesiones finalizadas con detalles de músculo y tiempos.
     */
    fun getAllCompletedMuscleSets(): Flow<List<com.example.gymtracker.data.local.relation.CompletedMuscleSetRecord>>
}
