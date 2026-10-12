package com.example.gymtracker.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymtracker.data.local.entity.ExerciseEntity
import com.example.gymtracker.data.local.entity.WorkoutSessionEntity
import com.example.gymtracker.data.local.entity.WorkoutSetEntity

/**
 * Representa una serie individual junto con toda la información del ejercicio correspondiente.
 */
data class WorkoutSetWithExercise(
    @Embedded
    val set: WorkoutSetEntity,

    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: ExerciseEntity
)

/**
 * Representa una sesión completa de entrenamiento (WorkoutSessionEntity)
 * con todos sus sets y los detalles de cada ejercicio asociado.
 */
data class WorkoutSessionWithDetails(
    @Embedded
    val session: WorkoutSessionEntity,

    @Relation(
        entity = WorkoutSetEntity::class,
        parentColumn = "id",
        entityColumn = "workoutSessionId"
    )
    val sets: List<WorkoutSetWithExercise>
) {
    /**
     * Helper para agrupar series por ejercicio manteniendo el orden de ejecución.
     */
    val setsGroupedByExercise: Map<ExerciseEntity, List<WorkoutSetEntity>>
        get() = sets
            .groupBy { it.exercise }
            .mapValues { entry -> entry.value.map { it.set }.sortedBy { it.setNumber } }
}
