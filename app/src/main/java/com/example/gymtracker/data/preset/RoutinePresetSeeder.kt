package com.example.gymtracker.data.preset

import android.util.Log
import com.example.gymtracker.data.local.AppDatabase
import com.example.gymtracker.data.local.dao.ExerciseDao
import com.example.gymtracker.data.local.dao.RoutineDao
import com.example.gymtracker.data.local.entity.ExerciseEntity
import com.example.gymtracker.data.local.entity.RoutineEntity

object RoutinePresetSeeder {

    private const val TAG = "RoutinePresetSeeder"

    suspend fun seedDefaultRoutines(database: AppDatabase, forceReload: Boolean = false): Int {
        return seedDefaultRoutines(database.routineDao(), database.exerciseDao(), forceReload)
    }

    /**
     * Comprueba y siembra las 6 rutinas por defecto (Día 1 a Día 6) si no están creadas.
     * Si [forceReload] es true, las recrea incluso si ya existen rutinas.
     */
    suspend fun seedDefaultRoutines(
        routineDao: RoutineDao,
        exerciseDao: ExerciseDao,
        forceReload: Boolean = false
    ): Int {
        var seededCount = 0

        try {
            for (preset in DefaultRoutinesData.allPresets.sortedBy { it.sortOrder }) {
                val existingRoutine = routineDao.getRoutineByTitle(preset.title)
                    ?: routineDao.getRoutineByDayPrefix("Día ${preset.dayNumber} —")
                if (existingRoutine != null && !forceReload) {
                    continue
                }

                // Asegurar que cada ejercicio exista en la tabla exercises respetando el orden estricto exerciseOrder ASC
                val exercisesSpec = mutableListOf<Triple<Long, Int, String>>()
                for (exPreset in preset.exercises.sortedBy { it.exerciseOrder }) {
                    val exercise = exerciseDao.getExerciseByName(exPreset.name)
                    // Mapear grupo muscular simplificado para filtros estándar si es necesario
                    val generalMuscle = when {
                        exPreset.muscleGroup.contains("Pectoral", ignoreCase = true) || exPreset.muscleGroup.contains("Pecho", ignoreCase = true) -> "Pecho"
                        exPreset.muscleGroup.contains("Deltoides", ignoreCase = true) || exPreset.muscleGroup.contains("Hombro", ignoreCase = true) -> "Hombros"
                        exPreset.muscleGroup.contains("Dorsal", ignoreCase = true) || exPreset.muscleGroup.contains("Espalda", ignoreCase = true) || exPreset.muscleGroup.contains("Romboides", ignoreCase = true) -> "Espalda"
                        exPreset.muscleGroup.contains("Bíceps", ignoreCase = true) || exPreset.muscleGroup.contains("Braquial", ignoreCase = true) -> "Bíceps"
                        exPreset.muscleGroup.contains("Tríceps", ignoreCase = true) -> "Tríceps"
                        exPreset.muscleGroup.contains("Cuádriceps", ignoreCase = true) -> "Cuádriceps"
                        exPreset.muscleGroup.contains("Isquio", ignoreCase = true) -> "Isquiotibiales"
                        exPreset.muscleGroup.contains("Glúteo", ignoreCase = true) || exPreset.muscleGroup.contains("Aductor", ignoreCase = true) -> "Piernas"
                        exPreset.muscleGroup.contains("Pantorrilla", ignoreCase = true) || exPreset.muscleGroup.contains("Gemelos", ignoreCase = true) -> "Pantorrillas"
                        exPreset.muscleGroup.contains("Abdominal", ignoreCase = true) || exPreset.muscleGroup.contains("Core", ignoreCase = true) -> "Core"
                        else -> exPreset.muscleGroup
                    }

                    val imageUrl = ExerciseImageProvider.getImageUrlForExercise(exPreset.name, generalMuscle)
                    val resolvedExerciseId = if (exercise == null) {
                        val newExercise = ExerciseEntity(
                            name = exPreset.name,
                            muscleGroup = generalMuscle,
                            equipment = exPreset.equipment,
                            isCustom = false,
                            defaultRestSeconds = if (exPreset.type == "Compuesto") 120 else 75,
                            primaryMuscle = exPreset.muscleGroup,
                            instructions = exPreset.technique,
                            thumbnailUrl = imageUrl,
                            gifPathOrUrl = imageUrl
                        )
                        exerciseDao.insertExercise(newExercise)
                    } else {
                        if (exercise.thumbnailUrl.isBlank() || exercise.gifPathOrUrl.isBlank()) {
                            exerciseDao.updateExercise(
                                exercise.copy(
                                    thumbnailUrl = if (exercise.thumbnailUrl.isBlank()) imageUrl else exercise.thumbnailUrl,
                                    gifPathOrUrl = if (exercise.gifPathOrUrl.isBlank()) imageUrl else exercise.gifPathOrUrl
                                )
                            )
                        }
                        exercise.id
                    }
                    exercisesSpec.add(Triple(resolvedExerciseId, exPreset.targetSets, exPreset.targetRepRange))
                }

                val notes = DefaultRoutinesData.generateRoutineNotes(preset)
                val routineEntity = RoutineEntity(
                    title = preset.title,
                    notes = notes,
                    createdAt = System.currentTimeMillis() - ((7 - preset.dayNumber) * 60_000L),
                    sortOrder = preset.sortOrder
                )

                if (existingRoutine != null && forceReload) {
                    routineDao.updateRoutineWithDetailedExercises(
                        routine = routineEntity.copy(id = existingRoutine.id, sortOrder = preset.sortOrder),
                        exercisesSpec = exercisesSpec
                    )
                } else {
                    routineDao.insertRoutineWithDetailedExercises(routineEntity, exercisesSpec)
                }

                seededCount++
            }
            Log.d(TAG, "Seeding v2 completado. Rutinas creadas/actualizadas: $seededCount")
        } catch (e: Exception) {
            Log.e(TAG, "Error sembrando rutinas por defecto", e)
        }

        return seededCount
    }
}
