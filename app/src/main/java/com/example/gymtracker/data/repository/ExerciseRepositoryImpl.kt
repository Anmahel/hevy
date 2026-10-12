package com.example.gymtracker.data.repository

import com.example.gymtracker.data.local.dao.ExerciseDao
import com.example.gymtracker.data.local.entity.ExerciseEntity
import com.example.gymtracker.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow

class ExerciseRepositoryImpl(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {

    override fun getAllExercises(): Flow<List<ExerciseEntity>> {
        return exerciseDao.getAllExercises()
    }

    override fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>> {
        return exerciseDao.getExercisesByMuscleGroup(muscleGroup)
    }

    override fun searchExercises(query: String): Flow<List<ExerciseEntity>> {
        return exerciseDao.searchExercisesByName(query)
    }

    override suspend fun getExerciseById(id: Long): ExerciseEntity? {
        return exerciseDao.getExerciseById(id)
    }

    override fun getExerciseFlowById(id: Long): Flow<ExerciseEntity?> {
        return exerciseDao.getExerciseFlowById(id)
    }

    override suspend fun insertCustomExercise(exercise: ExerciseEntity): Long {
        return exerciseDao.insertExercise(exercise.copy(isCustom = true))
    }

    override suspend fun updateExercise(exercise: ExerciseEntity) {
        exerciseDao.updateExercise(exercise)
    }

    override suspend fun deleteExercise(exercise: ExerciseEntity) {
        exerciseDao.deleteExercise(exercise)
    }

    override suspend fun getCount(): Int {
        return exerciseDao.getCount()
    }
}
