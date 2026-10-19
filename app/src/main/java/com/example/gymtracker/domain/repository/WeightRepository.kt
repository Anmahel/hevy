package com.example.gymtracker.domain.repository

import com.example.gymtracker.data.local.entity.CustomMealBlockEntity
import com.example.gymtracker.data.local.entity.DailyNutritionLogEntity
import com.example.gymtracker.data.local.entity.WeightLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de dominio para la gestión del registro de peso corporal, antropometría,
 * nutrición rápida 1-Tap y suplementación diaria.
 */
interface WeightRepository {
    fun getAllWeightLogs(): Flow<List<WeightLogEntity>>
    suspend fun getWeightLogById(id: Long): WeightLogEntity?
    suspend fun insertOrUpdateWeightLog(weightLog: WeightLogEntity): Long
    suspend fun updateWeightLog(weightLog: WeightLogEntity)
    suspend fun deleteWeightLog(weightLog: WeightLogEntity)
    suspend fun deleteWeightLogById(id: Long)

    // Módulo 3: Nutrición rápida 1-Tap y suplementación diaria
    fun observeAllDailyNutritionLogs(): Flow<List<DailyNutritionLogEntity>> = kotlinx.coroutines.flow.flowOf(emptyList())
    fun observeDailyNutritionLog(dateKey: String): Flow<DailyNutritionLogEntity?>
    suspend fun getDailyNutritionLog(dateKey: String): DailyNutritionLogEntity?
    suspend fun upsertDailyNutritionLog(log: DailyNutritionLogEntity)

    fun observeCustomMealBlocks(): Flow<List<CustomMealBlockEntity>>
    suspend fun insertCustomMealBlock(block: CustomMealBlockEntity): Long
    suspend fun deleteCustomMealBlockById(id: Long)
}
