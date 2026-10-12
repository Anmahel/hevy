package com.example.gymtracker.data.repository

import com.example.gymtracker.data.local.dao.ExerciseDao
import com.example.gymtracker.data.local.dao.RoutineDao
import com.example.gymtracker.data.local.entity.RoutineEntity
import com.example.gymtracker.data.local.entity.RoutineExerciseEntity
import com.example.gymtracker.data.local.relation.RoutineWithExercises
import com.example.gymtracker.data.local.relation.RoutineWithOrderedExercises
import com.example.gymtracker.data.preset.RoutinePresetSeeder
import com.example.gymtracker.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class RoutineRepositoryImpl(
    private val routineDao: RoutineDao,
    private val exerciseDao: ExerciseDao
) : RoutineRepository {

    override fun getAllRoutinesWithExercises(): Flow<List<RoutineWithExercises>> {
        return routineDao.getAllRoutinesWithExercises()
    }

    override fun getAllRoutinesWithOrderedExercises(): Flow<List<RoutineWithOrderedExercises>> {
        return routineDao.getAllRoutinesWithOrderedExercises()
    }

    override fun getRoutineById(routineId: Long): Flow<RoutineWithOrderedExercises?> {
        return routineDao.getRoutineWithOrderedExercises(routineId)
    }

    override suspend fun createRoutine(routine: RoutineEntity, exerciseIds: List<Long>): Long {
        return routineDao.insertRoutineWithExercises(routine, exerciseIds)
    }

    override suspend fun insertRoutine(routine: RoutineEntity): Long {
        return routineDao.insertRoutine(routine)
    }

    override suspend fun insertRoutineExercises(routineExercises: List<RoutineExerciseEntity>) {
        routineDao.insertRoutineExercises(routineExercises)
    }

    override suspend fun updateRoutine(routine: RoutineEntity) {
        routineDao.updateRoutine(routine)
    }

    override suspend fun updateRoutineWithExercises(
        routine: RoutineEntity,
        exerciseIds: List<Long>
    ) {
        routineDao.updateRoutineWithExercises(routine, exerciseIds)
    }

    override suspend fun duplicateRoutine(routineId: Long): Long {
        val existing = routineDao.getRoutineById(routineId) ?: return 0L
        val current = routineDao.getRoutineWithOrderedExercises(routineId).firstOrNull()
        val exerciseIds = current?.sortedExercises?.map { it.id } ?: emptyList()
        val duplicateEntity = RoutineEntity(
            title = "${existing.title} (Copia)",
            notes = existing.notes,
            createdAt = System.currentTimeMillis()
        )
        return routineDao.insertRoutineWithExercises(duplicateEntity, exerciseIds)
    }

    override suspend fun deleteRoutine(routineId: Long) {
        routineDao.deleteRoutineById(routineId)
    }

    override suspend fun seedDefaultRoutines(forceReload: Boolean): Int {
        return RoutinePresetSeeder.seedDefaultRoutines(routineDao, exerciseDao, forceReload)
    }
}
