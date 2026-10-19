package com.example.gymtracker.data.repository

import com.example.gymtracker.data.local.dao.WeightDao
import com.example.gymtracker.data.local.entity.CustomMealBlockEntity
import com.example.gymtracker.data.local.entity.DailyNutritionLogEntity
import com.example.gymtracker.data.local.entity.WeightLogEntity
import com.example.gymtracker.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementación de WeightRepository con Room SQLite.
 */
class WeightRepositoryImpl(
    private val weightDao: WeightDao
) : WeightRepository {

    override fun getAllWeightLogs(): Flow<List<WeightLogEntity>> {
        return weightDao.getAllWeightLogs()
    }

    override suspend fun getWeightLogById(id: Long): WeightLogEntity? {
        return weightDao.getWeightLogById(id)
    }

    override suspend fun insertOrUpdateWeightLog(weightLog: WeightLogEntity): Long {
        return weightDao.insertOrUpdateWeightLog(weightLog)
    }

    override suspend fun updateWeightLog(weightLog: WeightLogEntity) {
        weightDao.updateWeightLog(weightLog)
    }

    override suspend fun deleteWeightLog(weightLog: WeightLogEntity) {
        weightDao.deleteWeightLog(weightLog)
    }

    override suspend fun deleteWeightLogById(id: Long) {
        weightDao.deleteWeightLogById(id)
    }

    override fun observeAllDailyNutritionLogs(): Flow<List<DailyNutritionLogEntity>> {
        return weightDao.observeAllDailyNutritionLogs()
    }

    override fun observeDailyNutritionLog(dateKey: String): Flow<DailyNutritionLogEntity?> {
        return weightDao.observeDailyNutritionLog(dateKey)
    }

    override suspend fun getDailyNutritionLog(dateKey: String): DailyNutritionLogEntity? {
        return weightDao.getDailyNutritionLog(dateKey)
    }

    override suspend fun upsertDailyNutritionLog(log: DailyNutritionLogEntity) {
        weightDao.upsertDailyNutritionLog(log)
    }

    override fun observeCustomMealBlocks(): Flow<List<CustomMealBlockEntity>> {
        return weightDao.observeCustomMealBlocks()
    }

    override suspend fun insertCustomMealBlock(block: CustomMealBlockEntity): Long {
        return weightDao.insertCustomMealBlock(block)
    }

    override suspend fun deleteCustomMealBlockById(id: Long) {
        weightDao.deleteCustomMealBlockById(id)
    }
}
