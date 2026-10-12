package com.example.gymtracker.domain.repository

import com.example.gymtracker.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    /**
     * Emite la lista completa de ejercicios reactivamente.
     */
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    /**
     * Emite ejercicios filtrados por grupo muscular (ej. Pecho, Espalda, etc.).
     */
    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>>

    /**
     * Búsqueda reactiva por nombre de ejercicio.
     */
    fun searchExercises(query: String): Flow<List<ExerciseEntity>>

    /**
     * Obtiene un ejercicio específico por su ID.
     */
    suspend fun getExerciseById(id: Long): ExerciseEntity?

    /**
     * Observa un ejercicio específico reactivamente por su ID.
     */
    fun getExerciseFlowById(id: Long): Flow<ExerciseEntity?>

    /**
     * Inserta un ejercicio personalizado y retorna su ID generado.
     */
    suspend fun insertCustomExercise(exercise: ExerciseEntity): Long

    /**
     * Actualiza un ejercicio existente.
     */
    suspend fun updateExercise(exercise: ExerciseEntity)

    /**
     * Elimina un ejercicio.
     */
    suspend fun deleteExercise(exercise: ExerciseEntity)

    /**
     * Retorna el conteo total de ejercicios.
     */
    suspend fun getCount(): Int
}
