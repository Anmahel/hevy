package com.example.gymtracker.data.repository

import com.example.gymtracker.data.local.dao.ExerciseDao
import com.example.gymtracker.data.local.dao.WorkoutDao
import com.example.gymtracker.data.local.dao.WorkoutSetDao
import com.example.gymtracker.data.local.entity.ExerciseEntity
import com.example.gymtracker.data.local.entity.WorkoutEntity
import com.example.gymtracker.data.local.entity.WorkoutSessionEntity
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

    override suspend fun updateExercisePinnedNote(exerciseId: Long, note: String) {
        exerciseDao.updatePinnedNote(exerciseId, note)
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
                    },
                    exerciseOrder = setEntity.exerciseOrder,
                    targetRepRange = setEntity.targetRepRange
                )
            }

            // Group sets by exercise while strictly maintaining exerciseOrder ASC (and insertion id ASC)
            val groupedByExercise = domainSets.groupBy { it.exerciseId }
            val exerciseGroups = groupedByExercise
                .entries
                .sortedWith(
                    compareBy<Map.Entry<Long, List<WorkoutSet>>> { entry ->
                        entry.value.minOfOrNull { it.exerciseOrder } ?: 0
                    }.thenBy { entry ->
                        entry.value.minOfOrNull { it.id } ?: 0L
                    }
                )
                .mapNotNull { (exerciseId, sets) ->
                    val entity = exercisesMap[exerciseId]
                    if (entity != null) {
                        val lastSessionId = workoutDao.getLastCompletedSessionIdForExercise(exerciseId, workoutId)
                        val suggestion = if (lastSessionId != null) {
                            val prevSets = workoutDao.getSetsForExerciseInSession(lastSessionId, exerciseId)
                            val allCompleted = prevSets.isNotEmpty() && prevSets.all { it.isCompleted }
                            if (allCompleted) {
                                "🔥 Sugerencia hoy: +2.5 kg o +1 rep para sobrecarga progresiva"
                            } else null
                        } else null

                        val resolvedRepRange = sets.firstOrNull { it.targetRepRange.isNotBlank() }?.targetRepRange
                            ?: com.example.gymtracker.data.preset.DefaultRoutinesData.findExercisePreset(
                                workoutEntity.title,
                                entity.name
                            )?.targetRepRange
                            ?: ""

                        ExerciseSetsGroup(
                            exercise = entity.toDomain(),
                            sets = sets.sortedBy { s -> s.setNumber },
                            progressiveOverloadSuggestion = suggestion,
                            targetRepRange = resolvedRepRange
                        )
                    } else null
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
        val workoutId = workoutDao.insertWorkout(workout.toEntity())
        try {
            val sessionEntity = WorkoutSessionEntity(
                id = workoutId,
                routineId = null,
                title = workout.title,
                startTime = workout.startTimeMillis,
                endTime = workout.endTimeMillis,
                durationSeconds = 0,
                totalVolumeKg = workout.totalVolumeKg
            )
            workoutDao.insertWorkoutSession(sessionEntity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return workoutId
    }

    override suspend fun updateWorkout(workout: Workout) {
        workoutDao.updateWorkout(workout.toEntity())
        try {
            val existing = workoutDao.getWorkoutSessionById(workout.id)
            if (existing != null) {
                workoutDao.updateWorkoutSession(existing.copy(title = workout.title))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun finishWorkout(workoutId: Long, notes: String) {
        val now = System.currentTimeMillis()
        workoutDao.finishWorkout(workoutId, now, notes)
        try {
            val session = workoutDao.getWorkoutSessionById(workoutId)
            if (session != null) {
                val duration = ((now - session.startTime) / 1000).coerceAtLeast(0)
                workoutDao.finishWorkoutSession(workoutId, now, duration, session.totalVolumeKg)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deleteWorkout(workoutId: Long) {
        workoutDao.deleteWorkoutById(workoutId)
        try {
            workoutDao.deleteWorkoutSessionById(workoutId)
            workoutSetDao.deleteSetsForWorkout(workoutId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
                ExerciseEntity(
                    name = "Press de Banca con Barra",
                    muscleGroup = "CHEST",
                    equipment = "Barra",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Press de banca plano con barra", "pecho"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Press de banca plano con barra", "pecho"),
                    primaryMuscle = "Pectoral mayor"
                ),
                ExerciseEntity(
                    name = "Press Inclinado con Mancuernas",
                    muscleGroup = "CHEST",
                    equipment = "Mancuernas",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Press inclinado con mancuernas", "pecho"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Press inclinado con mancuernas", "pecho"),
                    primaryMuscle = "Pectoral superior"
                ),
                ExerciseEntity(
                    name = "Aperturas en Polea (Crossover)",
                    muscleGroup = "CHEST",
                    equipment = "Polea",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Aperturas en polea (crossover)", "pecho"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Aperturas en polea (crossover)", "pecho"),
                    primaryMuscle = "Pectoral mayor"
                ),
                ExerciseEntity(
                    name = "Fondos en Paralelas",
                    muscleGroup = "CHEST",
                    equipment = "Peso corporal",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Fondos en paralelas (dips pecho)", "pecho"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Fondos en paralelas (dips pecho)", "pecho"),
                    primaryMuscle = "Pectoral inferior"
                ),
                ExerciseEntity(
                    name = "Sentadilla con Barra (Back Squat)",
                    muscleGroup = "LEGS",
                    equipment = "Barra",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Sentadilla trasera con barra (back squat)", "cuádriceps"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Sentadilla trasera con barra (back squat)", "cuádriceps"),
                    primaryMuscle = "Cuádriceps"
                ),
                ExerciseEntity(
                    name = "Prensa de Piernas 45°",
                    muscleGroup = "LEGS",
                    equipment = "Máquina",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Prensa de piernas 45°", "cuádriceps"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Prensa de piernas 45°", "cuádriceps"),
                    primaryMuscle = "Cuádriceps"
                ),
                ExerciseEntity(
                    name = "Peso Muerto Rumano",
                    muscleGroup = "LEGS",
                    equipment = "Barra",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Peso muerto rumano con barra", "isquiotibiales"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Peso muerto rumano con barra", "isquiotibiales"),
                    primaryMuscle = "Isquiotibiales"
                ),
                ExerciseEntity(
                    name = "Extensión de Cuádriceps",
                    muscleGroup = "LEGS",
                    equipment = "Máquina",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Extensión de cuádriceps en máquina", "cuádriceps"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Extensión de cuádriceps en máquina", "cuádriceps"),
                    primaryMuscle = "Cuádriceps"
                ),
                ExerciseEntity(
                    name = "Curl Femoral Tumbado",
                    muscleGroup = "LEGS",
                    equipment = "Máquina",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Curl femoral tumbado en máquina", "isquiotibiales"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Curl femoral tumbado en máquina", "isquiotibiales"),
                    primaryMuscle = "Isquiotibiales"
                ),
                ExerciseEntity(
                    name = "Elevación de Gemelos en Máquina",
                    muscleGroup = "LEGS",
                    equipment = "Máquina",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Elevación de talones de pie en máquina", "gemelos"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Elevación de talones de pie en máquina", "gemelos"),
                    primaryMuscle = "Gemelos"
                ),
                ExerciseEntity(
                    name = "Peso Muerto Convencional",
                    muscleGroup = "BACK",
                    equipment = "Barra",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Peso muerto convencional", "espalda"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Peso muerto convencional", "espalda"),
                    primaryMuscle = "Espalda baja / Isquios"
                ),
                ExerciseEntity(
                    name = "Dominadas (Pull-ups)",
                    muscleGroup = "BACK",
                    equipment = "Peso corporal",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Dominadas pronas (pull-ups)", "espalda"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Dominadas pronas (pull-ups)", "espalda"),
                    primaryMuscle = "Dorsal ancho"
                ),
                ExerciseEntity(
                    name = "Remo con Barra",
                    muscleGroup = "BACK",
                    equipment = "Barra",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Remo con barra (pendlay)", "espalda"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Remo con barra (pendlay)", "espalda"),
                    primaryMuscle = "Dorsal y Romboides"
                ),
                ExerciseEntity(
                    name = "Jalón al Pecho en Polea",
                    muscleGroup = "BACK",
                    equipment = "Polea",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Jalón al pecho en polea", "espalda"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Jalón al pecho en polea", "espalda"),
                    primaryMuscle = "Dorsal ancho"
                ),
                ExerciseEntity(
                    name = "Remo en Polea Baja",
                    muscleGroup = "BACK",
                    equipment = "Polea",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Remo en polea baja (gironda)", "espalda"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Remo en polea baja (gironda)", "espalda"),
                    primaryMuscle = "Dorsal y Trapecio medio"
                ),
                ExerciseEntity(
                    name = "Press Militar con Barra (OHP)",
                    muscleGroup = "SHOULDERS",
                    equipment = "Barra",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Press militar de pie (ohp)", "hombros"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Press militar de pie (ohp)", "hombros"),
                    primaryMuscle = "Deltoides anterior"
                ),
                ExerciseEntity(
                    name = "Elevaciones Laterales con Mancuernas",
                    muscleGroup = "SHOULDERS",
                    equipment = "Mancuernas",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Elevaciones laterales con mancuernas", "hombros"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Elevaciones laterales con mancuernas", "hombros"),
                    primaryMuscle = "Deltoides lateral"
                ),
                ExerciseEntity(
                    name = "Pájaros posteriores en Polea",
                    muscleGroup = "SHOULDERS",
                    equipment = "Polea",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Pájaros posteriores con mancuernas", "hombros"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Pájaros posteriores con mancuernas", "hombros"),
                    primaryMuscle = "Deltoides posterior"
                ),
                ExerciseEntity(
                    name = "Curl de Bíceps con Barra Z",
                    muscleGroup = "ARMS",
                    equipment = "Barra Z",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Curl de bíceps con barra z", "bíceps"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Curl de bíceps con barra z", "bíceps"),
                    primaryMuscle = "Bíceps braquial"
                ),
                ExerciseEntity(
                    name = "Curl Martillo con Mancuernas",
                    muscleGroup = "ARMS",
                    equipment = "Mancuernas",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Curl martillo con mancuernas", "bíceps"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Curl martillo con mancuernas", "bíceps"),
                    primaryMuscle = "Braquiorradial"
                ),
                ExerciseEntity(
                    name = "Extensiones de Tríceps en Polea Alta",
                    muscleGroup = "ARMS",
                    equipment = "Polea",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Extensiones de tríceps en polea alta con cuerda", "tríceps"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Extensiones de tríceps en polea alta con cuerda", "tríceps"),
                    primaryMuscle = "Tríceps braquial"
                ),
                ExerciseEntity(
                    name = "Press Francés con Barra Z",
                    muscleGroup = "ARMS",
                    equipment = "Barra Z",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Press francés con barra z", "tríceps"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Press francés con barra z", "tríceps"),
                    primaryMuscle = "Tríceps (cabeza larga)"
                ),
                ExerciseEntity(
                    name = "Elevación de Piernas Colgado",
                    muscleGroup = "CORE",
                    equipment = "Barra",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Elevaciones de piernas colgado en barra", "core"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Elevaciones de piernas colgado en barra", "core"),
                    primaryMuscle = "Abdomen inferior"
                ),
                ExerciseEntity(
                    name = "Plancha Abdominal",
                    muscleGroup = "CORE",
                    equipment = "Esterilla",
                    thumbnailUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Plancha abdominal isométrica", "core"),
                    gifPathOrUrl = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise("Plancha abdominal isométrica", "core"),
                    primaryMuscle = "Core / Recto abdominal"
                )
            )
            exerciseDao.insertExercises(defaults)
        } else {
            // Enriquecer ejercicios existentes que no tengan URL
            val existing = exerciseDao.getAllExercisesList()
            for (ex in existing) {
                if (ex.thumbnailUrl.isBlank() || ex.gifPathOrUrl.isBlank()) {
                    val url = com.example.gymtracker.data.preset.ExerciseImageProvider.getImageUrlForExercise(ex.name, ex.muscleGroup)
                    exerciseDao.updateExercise(
                        ex.copy(
                            thumbnailUrl = if (ex.thumbnailUrl.isBlank()) url else ex.thumbnailUrl,
                            gifPathOrUrl = if (ex.gifPathOrUrl.isBlank()) url else ex.gifPathOrUrl
                        )
                    )
                }
            }
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
        instructions = instructions,
        pinnedNote = pinnedNote
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
        instructions = instructions,
        pinnedNote = pinnedNote
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
        setType = try { SetType.valueOf(setType.uppercase()) } catch (e: Exception) { SetType.NORMAL },
        exerciseOrder = exerciseOrder,
        targetRepRange = targetRepRange
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
        setType = setType.name,
        exerciseOrder = exerciseOrder,
        targetRepRange = targetRepRange
    )
}
