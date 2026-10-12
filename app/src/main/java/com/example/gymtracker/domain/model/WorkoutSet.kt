package com.example.gymtracker.domain.model

enum class SetType(val label: String) {
    NORMAL("Normal"),
    WARMUP("Calentamiento"),
    DROPSET("Drop Set"),
    FAILURE("Fallo")
}

data class WorkoutSet(
    val id: Long = 0,
    val workoutId: Long,
    val exerciseId: Long,
    val exerciseName: String = "",
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val isCompleted: Boolean = false,
    val rpe: Double? = null,
    val setType: SetType = SetType.NORMAL,
    val previousRecord: String = "-"
)
