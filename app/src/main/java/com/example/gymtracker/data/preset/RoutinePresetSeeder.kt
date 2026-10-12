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
            for (preset in DefaultRoutinesData.allPresets) {
                val existingRoutine = routineDao.getRoutineByTitle(preset.title)
                if (existingRoutine != null && !forceReload) {
                    continue
                }

                // Asegurar que cada ejercicio exista en la tabla exercises
                val exerciseIds = mutableListOf<Long>()
                for (exPreset in preset.exercises) {
                    var exercise = exerciseDao.getExerciseByName(exPreset.name)
                    if (exercise == null) {
                        // Mapear grupo muscular simplificado para filtros estándar si es necesario
                        val generalMuscle = when {
                            exPreset.muscleGroup.contains("Pectoral", ignoreCase = true) -> "Pecho"
                            exPreset.muscleGroup.contains("Deltoides", ignoreCase = true) || exPreset.muscleGroup.contains("Hombro", ignoreCase = true) -> "Hombros"
                            exPreset.muscleGroup.contains("Dorsal", ignoreCase = true) || exPreset.muscleGroup.contains("Espalda", ignoreCase = true) || exPreset.muscleGroup.contains("Romboides", ignoreCase = true) -> "Espalda"
                            exPreset.muscleGroup.contains("Bíceps", ignoreCase = true) || exPreset.muscleGroup.contains("Braquial", ignoreCase = true) -> "Bíceps"
                            exPreset.muscleGroup.contains("Tríceps", ignoreCase = true) -> "Tríceps"
                            exPreset.muscleGroup.contains("Cuádriceps", ignoreCase = true) -> "Cuádriceps"
                            exPreset.muscleGroup.contains("Isquio", ignoreCase = true) -> "Isquios"
                            exPreset.muscleGroup.contains("Glúteo", ignoreCase = true) -> "Piernas"
                            exPreset.muscleGroup.contains("Pantorrilla", ignoreCase = true) || exPreset.muscleGroup.contains("Gemelos", ignoreCase = true) -> "Gemelos"
                            exPreset.muscleGroup.contains("Abdominal", ignoreCase = true) || exPreset.muscleGroup.contains("Core", ignoreCase = true) -> "Abdomen"
                            else -> exPreset.muscleGroup
                        }

                        val newExercise = ExerciseEntity(
                            name = exPreset.name,
                            muscleGroup = generalMuscle,
                            equipment = exPreset.equipment,
                            isCustom = false,
                            defaultRestSeconds = if (exPreset.type == "Compuesto") 120 else 75,
                            primaryMuscle = exPreset.muscleGroup,
                            instructions = exPreset.technique
                        )
                        val insertedId = exerciseDao.insertExercise(newExercise)
                        exerciseIds.add(insertedId)
                    } else {
                        exerciseIds.add(exercise.id)
                    }
                }

                val notes = DefaultRoutinesData.generateRoutineNotes(preset)
                val routineEntity = RoutineEntity(
                    title = preset.title,
                    notes = notes,
                    createdAt = System.currentTimeMillis() - ((7 - preset.dayNumber) * 60_000L) // Orden cronológico Día 1 primero
                )

                if (existingRoutine != null && forceReload) {
                    routineDao.updateRoutineWithExercises(
                        routine = routineEntity.copy(id = existingRoutine.id),
                        exerciseIds = exerciseIds
                    )
                } else {
                    routineDao.insertRoutineWithExercises(routineEntity, exerciseIds)
                }

                seededCount++
            }
            Log.d(TAG, "Seeding completado. Rutinas creadas/actualizadas: $seededCount")
        } catch (e: Exception) {
            Log.e(TAG, "Error sembrando rutinas por defecto", e)
        }

        return seededCount
    }
}
