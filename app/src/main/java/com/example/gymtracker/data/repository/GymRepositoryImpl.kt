package com.example.gymtracker.data.repository

import com.example.gymtracker.data.local.dao.ExerciseDao
import com.example.gymtracker.data.local.dao.WorkoutDao
import com.example.gymtracker.data.local.dao.WorkoutSetDao
import com.example.gymtracker.data.local.entity.ExerciseEntity
import com.example.gymtracker.data.local.entity.WorkoutEntity
import com.example.gymtracker.data.local.entity.WorkoutSetEntity
import com.example.gymtracker.domain.model.Exercise
import com.example.gymtracker.domain.model.ExerciseSetsGroup
import com.example.gymtracker.domain.model.MuscleGroup
import com.example.gymtracker.domain.model.SetType
import com.example.gymtracker.domain.model.Workout
import com.example.gymtracker.domain.model.WorkoutSet
import com.example.gymtracker.domain.model.WorkoutWithSets
import com.example.gymtracker.domain.repository.GymRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class GymRepositoryImpl(
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao,
    private val workoutSetDao: WorkoutSetDao
) : GymRepository {

    override fun getAllExercises(): Flow<List<Exercise>> {
        return exerciseDao.getAllExercises().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getExerciseById(id: Long): Exercise? {
        return exerciseDao.getExerciseById(id)?.toDomain()
    }

    override suspend fun insertExercise(exercise: Exercise): Long {
        return exerciseDao.insertExercise(exercise.toEntity())
    }

    override fun getAllWorkouts(): Flow<List<Workout>> {
        return workoutDao.getAllWorkouts().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getActiveWorkout(): Flow<Workout?> {
        return workoutDao.getActiveWorkout().map { it?.toDomain() }
    }

    override fun getWorkoutWithSets(workoutId: Long): Flow<WorkoutWithSets?> {
        return combine(
            workoutDao.getWorkoutByIdFlow(workoutId),
            workoutSetDao.getSetsForWorkout(workoutId),
            exerciseDao.getAllExercises()
        ) { workoutEntity, setEntities, exerciseEntities ->
            if (workoutEntity == null) return@combine null

            val exercisesMap = exerciseEntities.associateBy { it.id }
            val domainSets = setEntities.map { setEntity ->
                val ex = exercisesMap[setEntity.exerciseId]
                val prevPerf = workoutDao.getPreviousSetPerformance(setEntity.exerciseId, setEntity.setNumber, workoutId)
                    ?: workoutDao.getLastExercisePerformance(setEntity.exerciseId, workoutId)
                    ?: "-"
                WorkoutSet(
                    id = setEntity.id,
                    workoutId = setEntity.workoutSessionId,
                    exerciseId = setEntity.exerciseId,
                    exerciseName = ex?.name ?: "Ejercicio desconocido",
                    setNumber = setEntity.setNumber,
                    weightKg = setEntity.weightKg,
                    reps = setEntity.reps,
                    isCompleted = setEntity.isCompleted,
                    rpe = setEntity.rpe,
                    previousRecord = prevPerf,
                    setType = try {
                        SetType.valueOf(setEntity.setType.uppercase())
                    } catch (e: Exception) {
                        SetType.NORMAL
                    }
                )
            }

            // Group sets by exercise while maintaining insertion order
            val groupedByExercise = domainSets.groupBy { it.exerciseId }
            val exerciseGroups = groupedByExercise.mapNotNull { (exerciseId, sets) ->
                val entity = exercisesMap[exerciseId]
                entity?.let {
                    ExerciseSetsGroup(
                        exercise = it.toDomain(),
                        sets = sets.sortedBy { s -> s.setNumber }
                    )
                }
            }

            // Calculate total volume and sets
            val totalVolume = domainSets.filter { it.isCompleted }.sumOf { it.weightKg * it.reps }
            val totalCompletedSets = domainSets.count { it.isCompleted }

            WorkoutWithSets(
                workout = workoutEntity.toDomain().copy(
                    totalVolumeKg = totalVolume,
                    totalSets = totalCompletedSets
                ),
                exercises = exerciseGroups
            )
        }
    }

    override suspend fun insertWorkout(workout: Workout): Long {
        return workoutDao.insertWorkout(workout.toEntity())
    }

    override suspend fun updateWorkout(workout: Workout) {
        workoutDao.updateWorkout(workout.toEntity())
    }

    override suspend fun finishWorkout(workoutId: Long, notes: String) {
        workoutDao.finishWorkout(workoutId, System.currentTimeMillis(), notes)
    }

    override suspend fun deleteWorkout(workoutId: Long) {
        workoutDao.deleteWorkoutById(workoutId)
    }

    override fun getSetsForWorkout(workoutId: Long): Flow<List<WorkoutSet>> {
        return workoutSetDao.getSetsForWorkout(workoutId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertSet(set: WorkoutSet): Long {
        return workoutSetDao.insertSet(set.toEntity())
    }

    override suspend fun updateSet(set: WorkoutSet) {
        workoutSetDao.updateSet(set.toEntity())
    }

    override suspend fun deleteSet(setId: Long) {
        workoutSetDao.deleteSetById(setId)
    }

    override suspend fun getPreviousPerformance(
        exerciseId: Long,
        setNumber: Int,
        currentWorkoutId: Long
    ): String {
        return workoutDao.getPreviousSetPerformance(exerciseId, setNumber, currentWorkoutId)
            ?: workoutDao.getLastExercisePerformance(exerciseId, currentWorkoutId)
            ?: "-"
    }

    override suspend fun prepopulateDefaultExercises() {
        if (exerciseDao.countExercises() == 0) {
            val defaults = listOf(
                ExerciseEntity(name = "Press de Banca con Barra", muscleGroup = "CHEST", equipment = "Barra"),
                ExerciseEntity(name = "Press Inclinado con Mancuernas", muscleGroup = "CHEST", equipment = "Mancuernas"),
                ExerciseEntity(name = "Aperturas en Polea (Crossover)", muscleGroup = "CHEST", equipment = "Polea"),
                ExerciseEntity(name = "Fondos en Paralelas", muscleGroup = "CHEST", equipment = "Peso corporal"),
                ExerciseEntity(name = "Sentadilla con Barra (Back Squat)", muscleGroup = "LEGS", equipment = "Barra"),
                ExerciseEntity(name = "Prensa de Piernas 45°", muscleGroup = "LEGS", equipment = "Máquina"),
                ExerciseEntity(name = "Peso Muerto Rumano", muscleGroup = "LEGS", equipment = "Barra"),
                ExerciseEntity(name = "Extensión de Cuádriceps", muscleGroup = "LEGS", equipment = "Máquina"),
                ExerciseEntity(name = "Curl Femoral Tumbado", muscleGroup = "LEGS", equipment = "Máquina"),
                ExerciseEntity(name = "Elevación de Gemelos en Máquina", muscleGroup = "LEGS", equipment = "Máquina"),
                ExerciseEntity(name = "Peso Muerto Convencional", muscleGroup = "BACK", equipment = "Barra"),
                ExerciseEntity(name = "Dominadas (Pull-ups)", muscleGroup = "BACK", equipment = "Peso corporal"),
                ExerciseEntity(name = "Remo con Barra", muscleGroup = "BACK", equipment = "Barra"),
                ExerciseEntity(name = "Jalón al Pecho en Polea", muscleGroup = "BACK", equipment = "Polea"),
                ExerciseEntity(name = "Remo en Polea Baja", muscleGroup = "BACK", equipment = "Polea"),
                ExerciseEntity(name = "Press Militar con Barra (OHP)", muscleGroup = "SHOULDERS", equipment = "Barra"),
                ExerciseEntity(name = "Elevaciones Laterales con Mancuernas", muscleGroup = "SHOULDERS", equipment = "Mancuernas"),
                ExerciseEntity(name = "Pájaros posteriores en Polea", muscleGroup = "SHOULDERS", equipment = "Polea"),
                ExerciseEntity(name = "Curl de Bíceps con Barra Z", muscleGroup = "ARMS", equipment = "Barra Z"),
                ExerciseEntity(name = "Curl Martillo con Mancuernas", muscleGroup = "ARMS", equipment = "Mancuernas"),
                ExerciseEntity(name = "Extensiones de Tríceps en Polea Alta", muscleGroup = "ARMS", equipment = "Polea"),
                ExerciseEntity(name = "Press Francés con Barra Z", muscleGroup = "ARMS", equipment = "Barra Z"),
                ExerciseEntity(name = "Elevación de Piernas Colgado", muscleGroup = "CORE", equipment = "Barra"),
                ExerciseEntity(name = "Plancha Abdominal", muscleGroup = "CORE", equipment = "Esterilla")
            )
            exerciseDao.insertExercises(defaults)
        }
    }

    // Mapper extensions
    private fun ExerciseEntity.toDomain() = Exercise(
        id = id,
        name = name,
        muscleGroup = MuscleGroup.fromString(muscleGroup),
        equipment = equipment,
        defaultRestSeconds = defaultRestSeconds,
        gifPathOrUrl = gifPathOrUrl,
        thumbnailUrl = thumbnailUrl,
        primaryMuscle = primaryMuscle.ifEmpty { muscleGroup },
        secondaryMuscles = secondaryMuscles,
        instructions = instructions
    )

    private fun Exercise.toEntity() = ExerciseEntity(
        id = id,
        name = name,
        muscleGroup = muscleGroup.name,
        equipment = equipment,
        defaultRestSeconds = defaultRestSeconds,
        gifPathOrUrl = gifPathOrUrl,
        thumbnailUrl = thumbnailUrl,
        primaryMuscle = primaryMuscle,
        secondaryMuscles = secondaryMuscles,
        instructions = instructions
    )

    private fun WorkoutEntity.toDomain() = Workout(
        id = id,
        title = title,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        notes = notes,
        totalVolumeKg = totalVolumeKg,
        totalSets = totalSets
    )

    private fun Workout.toEntity() = WorkoutEntity(
        id = id,
        title = title,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        notes = notes,
        totalVolumeKg = totalVolumeKg,
        totalSets = totalSets
    )

    private fun WorkoutSetEntity.toDomain() = WorkoutSet(
        id = id,
        workoutId = workoutSessionId,
        exerciseId = exerciseId,
        setNumber = setNumber,
        weightKg = weightKg,
        reps = reps,
        isCompleted = isCompleted,
        rpe = rpe,
        setType = try { SetType.valueOf(setType.uppercase()) } catch (e: Exception) { SetType.NORMAL }
    )

    private fun WorkoutSet.toEntity() = WorkoutSetEntity(
        id = id,
        workoutSessionId = workoutId,
        exerciseId = exerciseId,
        setNumber = setNumber,
        weightKg = weightKg,
        reps = reps,
        isCompleted = isCompleted,
        rpe = rpe,
        setType = setType.name
    )
}
