package com.example.gymtracker.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.gymtracker.data.local.entity.ExerciseEntity
import com.example.gymtracker.data.local.entity.RoutineEntity
import com.example.gymtracker.data.local.entity.RoutineExerciseEntity

/**
 * Consulta de una rutina junto con sus ejercicios ordenados.
 * 
 * En Room, cuando la tabla intermedia (RoutineExerciseEntity) contiene datos adicionales
 * (como `orderIndex`), se utiliza la estructura compuesta RoutineExerciseWithDetails
 * para tener acceso tanto al orden como a la entidad completa del ejercicio.
 */
data class RoutineExerciseWithDetails(
    @Embedded
    val routineExercise: RoutineExerciseEntity,

    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: ExerciseEntity
)

/**
 * Rutina con sus ejercicios asociados conservando el índice de orden (orderIndex).
 */
data class RoutineWithOrderedExercises(
    @Embedded
    val routine: RoutineEntity,

    @Relation(
        entity = RoutineExerciseEntity::class,
        parentColumn = "id",
        entityColumn = "routineId"
    )
    val routineExercises: List<RoutineExerciseWithDetails>
) {
    /**
     * Lista de relaciones ya ordenadas estrictamente por exerciseOrder ASC (y orderIndex ASC).
     */
    val sortedRoutineExercises: List<RoutineExerciseWithDetails>
        get() = routineExercises.sortedWith(
            compareBy<RoutineExerciseWithDetails> { it.routineExercise.exerciseOrder }
                .thenBy { it.routineExercise.orderIndex }
                .thenBy { it.routineExercise.id }
        )

    /**
     * Lista conveniente de ejercicios ya ordenados por exerciseOrder ASC.
     */
    val sortedExercises: List<ExerciseEntity>
        get() = sortedRoutineExercises.map { it.exercise }
}

/**
 * Variante directa utilizando Room Junction (útil si solo se requiere la lista de ejercicios).
 */
data class RoutineWithExercises(
    @Embedded
    val routine: RoutineEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = RoutineExerciseEntity::class,
            parentColumn = "routineId",
            entityColumn = "exerciseId"
        )
    )
    val exercises: List<ExerciseEntity>
)
