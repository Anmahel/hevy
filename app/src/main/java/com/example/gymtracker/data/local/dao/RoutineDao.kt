package com.example.gymtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.gymtracker.data.local.entity.RoutineEntity
import com.example.gymtracker.data.local.entity.RoutineExerciseEntity
import com.example.gymtracker.data.local.relation.RoutineWithExercises
import com.example.gymtracker.data.local.relation.RoutineWithOrderedExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    /**
     * Inserta la cabecera de la rutina. Devuelve el ID generado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    /**
     * Inserta la lista de ejercicios asociados a la rutina (tabla intermedia con orderIndex).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercises(routineExercises: List<RoutineExerciseEntity>)

    /**
     * Transacción atómica de alto nivel: crea la rutina y vincula sus ejercicios en una sola operación.
     */
    @Transaction
    suspend fun insertRoutineWithExercises(
        routine: RoutineEntity,
        exerciseIds: List<Long>
    ): Long {
        val routineId = insertRoutine(routine)
        val crossRefs = exerciseIds.mapIndexed { index, exerciseId ->
            RoutineExerciseEntity(
                routineId = routineId,
                exerciseId = exerciseId,
                orderIndex = index
            )
        }
        insertRoutineExercises(crossRefs)
        return routineId
    }

    /**
     * Lista todas las rutinas completas con sus ejercicios asociados (utilizando Junction).
     */
    @Transaction
    @Query("SELECT * FROM routines ORDER BY createdAt DESC")
    fun getAllRoutinesWithExercises(): Flow<List<RoutineWithExercises>>

    /**
     * Obtiene una rutina específica con sus ejercicios utilizando Junction.
     */
    @Transaction
    @Query("SELECT * FROM routines WHERE id = :routineId LIMIT 1")
    fun getRoutineWithExercises(routineId: Long): Flow<RoutineWithExercises?>

    /**
     * Lista rutinas preservando el orden exacto (orderIndex) de cada ejercicio.
     */
    @Transaction
    @Query("SELECT * FROM routines ORDER BY createdAt DESC")
    fun getAllRoutinesWithOrderedExercises(): Flow<List<RoutineWithOrderedExercises>>

    /**
     * Obtiene una rutina específica con sus ejercicios ordenados por orderIndex.
     */
    @Transaction
    @Query("SELECT * FROM routines WHERE id = :routineId LIMIT 1")
    fun getRoutineWithOrderedExercises(routineId: Long): Flow<RoutineWithOrderedExercises?>

    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    suspend fun getRoutineById(id: Long): RoutineEntity?

    @Query("SELECT COUNT(*) FROM routines")
    suspend fun getRoutineCount(): Int

    @Query("SELECT * FROM routines WHERE LOWER(title) = LOWER(:title) LIMIT 1")
    suspend fun getRoutineByTitle(title: String): RoutineEntity?

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteRoutineExercisesByRoutineId(routineId: Long)

    /**
     * Actualiza la rutina y reemplaza sus ejercicios de forma atómica.
     */
    @Transaction
    suspend fun updateRoutineWithExercises(
        routine: RoutineEntity,
        exerciseIds: List<Long>
    ) {
        updateRoutine(routine)
        deleteRoutineExercisesByRoutineId(routine.id)
        val crossRefs = exerciseIds.mapIndexed { index, exerciseId ->
            RoutineExerciseEntity(
                routineId = routine.id,
                exerciseId = exerciseId,
                orderIndex = index
            )
        }
        insertRoutineExercises(crossRefs)
    }

    /**
     * Actualiza los datos principales de una rutina.
     */
    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    /**
     * Elimina una rutina por ID. Gracias a `onDelete = ForeignKey.CASCADE`,
     * SQLite elimina automáticamente todos los registros asociados en `routine_exercises`.
     */
    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutineById(id: Long)
}
