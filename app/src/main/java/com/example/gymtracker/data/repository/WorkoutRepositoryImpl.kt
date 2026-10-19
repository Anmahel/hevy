package com.example.gymtracker.data.repository

import com.example.gymtracker.data.local.dao.WorkoutDao
import com.example.gymtracker.data.local.entity.WorkoutSessionEntity
import com.example.gymtracker.data.local.entity.WorkoutSetEntity
import com.example.gymtracker.data.local.relation.WorkoutSessionWithDetails
import com.example.gymtracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow

class WorkoutRepositoryImpl(
    private val workoutDao: WorkoutDao
) : WorkoutRepository {

    override fun getWorkoutHistory(): Flow<List<WorkoutSessionWithDetails>> {
        return workoutDao.getWorkoutHistory()
    }

    override fun getWorkoutSessionById(sessionId: Long): Flow<WorkoutSessionWithDetails?> {
        return workoutDao.getWorkoutSessionWithDetails(sessionId)
    }

    override fun getActiveSession(): Flow<WorkoutSessionEntity?> {
        return workoutDao.getActiveSession()
    }

    override suspend fun saveWorkoutSession(
        session: WorkoutSessionEntity,
        sets: List<WorkoutSetEntity>
    ): Long {
        return workoutDao.saveWorkoutSessionWithSets(session, sets)
    }

    override suspend fun updateWorkoutSession(session: WorkoutSessionEntity) {
        workoutDao.updateWorkoutSession(session)
    }

    override suspend fun finishWorkoutSession(
        sessionId: Long,
        endTime: Long,
        durationSeconds: Long,
        totalVolumeKg: Double
    ) {
        workoutDao.finishWorkoutSession(
            sessionId = sessionId,
            endTime = endTime,
            durationSeconds = durationSeconds,
            totalVolumeKg = totalVolumeKg
        )
    }

    override suspend fun deleteWorkoutSession(sessionId: Long) {
        workoutDao.deleteWorkoutSessionById(sessionId)
        try {
            workoutDao.deleteWorkoutById(sessionId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getAllCompletedSessions(): Flow<List<WorkoutSessionEntity>> {
        return workoutDao.getAllCompletedSessions()
    }

    override fun getCompletedSessionsInRange(startMillis: Long, endMillis: Long): Flow<List<WorkoutSessionWithDetails>> {
        return workoutDao.getCompletedSessionsInRange(startMillis, endMillis)
    }

    override fun countSessionsInRange(startMillis: Long, endMillis: Long): Flow<Int> {
        return workoutDao.countSessionsInRange(startMillis, endMillis)
    }

    override fun getAllCompletedMuscleSets(): Flow<List<com.example.gymtracker.data.local.relation.CompletedMuscleSetRecord>> {
        return workoutDao.getAllCompletedMuscleSets()
    }
}
