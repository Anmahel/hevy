package com.example.gymtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gymtracker.data.local.entity.CustomMealBlockEntity
import com.example.gymtracker.data.local.entity.DailyNutritionLogEntity
import com.example.gymtracker.data.local.entity.WeightLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para las operaciones de WeightLogEntity, DailyNutritionLogEntity
 * y CustomMealBlockEntity.
 */
@Dao
interface WeightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWeightLog(weightLog: WeightLogEntity): Long

    @Update
    suspend fun updateWeightLog(weightLog: WeightLogEntity)

    @Query("SELECT * FROM weight_logs ORDER BY date DESC")
    fun getAllWeightLogs(): Flow<List<WeightLogEntity>>

    @Query("SELECT * FROM weight_logs WHERE id = :id LIMIT 1")
    suspend fun getWeightLogById(id: Long): WeightLogEntity?

    @Delete
    suspend fun deleteWeightLog(weightLog: WeightLogEntity)

    @Query("DELETE FROM weight_logs WHERE id = :id")
    suspend fun deleteWeightLogById(id: Long)

    // --- Módulo 3: Registro Rápido de Proteína 1-Tap y Suplementación Diaria ---

    @Query("SELECT * FROM daily_nutrition_logs ORDER BY dateKey DESC")
    fun observeAllDailyNutritionLogs(): Flow<List<DailyNutritionLogEntity>>

    @Query("SELECT * FROM daily_nutrition_logs WHERE dateKey = :dateKey LIMIT 1")
    fun observeDailyNutritionLog(dateKey: String): Flow<DailyNutritionLogEntity?>

    @Query("SELECT * FROM daily_nutrition_logs WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getDailyNutritionLog(dateKey: String): DailyNutritionLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyNutritionLog(log: DailyNutritionLogEntity)

    @Query("SELECT * FROM custom_meal_blocks ORDER BY id ASC")
    fun observeCustomMealBlocks(): Flow<List<CustomMealBlockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomMealBlock(block: CustomMealBlockEntity): Long

    @Query("DELETE FROM custom_meal_blocks WHERE id = :id")
    suspend fun deleteCustomMealBlockById(id: Long)
}
