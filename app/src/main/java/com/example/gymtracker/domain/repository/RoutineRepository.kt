package com.example.gymtracker.domain.repository

import com.example.gymtracker.data.local.entity.RoutineEntity
import com.example.gymtracker.data.local.entity.RoutineExerciseEntity
import com.example.gymtracker.data.local.relation.RoutineWithExercises
import com.example.gymtracker.data.local.relation.RoutineWithOrderedExercises
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    /**
     * Emite todas las rutinas registradas junto con sus ejercicios asociados (via Junction).
     */
    fun getAllRoutinesWithExercises(): Flow<List<RoutineWithExercises>>

    /**
     * Emite todas las rutinas preservando el orden exacto (orderIndex) de cada ejercicio.
     */
    fun getAllRoutinesWithOrderedExercises(): Flow<List<RoutineWithOrderedExercises>>

    /**
     * Emite una rutina específica con sus ejercicios ordenados.
     */
    fun getRoutineById(routineId: Long): Flow<RoutineWithOrderedExercises?>

    /**
     * Crea una nueva rutina y asocia sus ejercicios en una transacción atómica.
     */
    suspend fun createRoutine(routine: RoutineEntity, exerciseIds: List<Long>): Long

    /**
     * Inserta directamente una entidad de rutina simple.
     */
    suspend fun insertRoutine(routine: RoutineEntity): Long

    /**
     * Inserta los vínculos de ejercicios de una rutina.
     */
    suspend fun insertRoutineExercises(routineExercises: List<RoutineExerciseEntity>)

    /**
     * Actualiza los datos de una rutina.
     */
    suspend fun updateRoutine(routine: RoutineEntity)

    /**
     * Actualiza la rutina y sus ejercicios vinculados en una transacción atómica.
     */
    suspend fun updateRoutineWithExercises(routine: RoutineEntity, exerciseIds: List<Long>)

    /**
     * Duplica una rutina existente junto con todos sus ejercicios.
     */
    suspend fun duplicateRoutine(routineId: Long): Long

    /**
     * Elimina una rutina por ID (limpieza en cascada en SQLite).
     */
    suspend fun deleteRoutine(routineId: Long)

    /**
     * Siembra o restaura las 6 rutinas por defecto (Día 1 a Día 6).
     */
    suspend fun seedDefaultRoutines(forceReload: Boolean = false): Int
}
