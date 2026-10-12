package com.example.gymtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gymtracker.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    /**
     * Obtiene todos los ejercicios ordenados por grupo muscular y nombre.
     */
    @Query("SELECT * FROM exercises ORDER BY muscleGroup ASC, name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    /**
     * Filtra ejercicios por grupo muscular específico.
     */
    @Query("SELECT * FROM exercises WHERE muscleGroup = :muscleGroup ORDER BY name ASC")
    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>>

    /**
     * Busca ejercicios por coincidencia parcial de nombre (ignorando mayúsculas/minúsculas).
     */
    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchExercisesByName(query: String): Flow<List<ExerciseEntity>>

    /**
     * Obtiene un ejercicio específico por su ID.
     */
    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    suspend fun getExerciseById(id: Long): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    fun getExerciseFlowById(id: Long): Flow<ExerciseEntity?>

    /**
     * Obtiene un ejercicio por coincidencia exacta de nombre.
     */
    @Query("SELECT * FROM exercises WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getExerciseByName(name: String): ExerciseEntity?

    /**
     * Cuenta total de ejercicios almacenados (útil para verificar pre-poblado).
     */
    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun countExercises(): Int

    /**
     * Inserta un ejercicio (por ejemplo, personalizado creado por el usuario).
     * Devuelve el ID generado por SQLite.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    /**
     * Inserción masiva de ejercicios (usado en el pre-poblado inicial desde JSON).
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    /**
     * Actualiza un ejercicio existente.
     */
    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    /**
     * Elimina un ejercicio.
     */
    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)

    /**
     * Elimina un ejercicio por ID.
     */
    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun deleteExerciseById(id: Long)
}
