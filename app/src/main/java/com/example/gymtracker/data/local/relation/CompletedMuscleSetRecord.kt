package com.example.gymtracker.data.local.relation

/**
 * Registro de una serie completada junto a los datos del ejercicio y la sesión a la que pertenece.
 * Permite analizar el volumen semanal por grupo muscular y el tiempo de recuperación sin consultas N+1.
 */
data class CompletedMuscleSetRecord(
    val setId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val muscleGroup: String,
    val primaryMuscle: String,
    val secondaryMuscles: String = "",
    val weightKg: Double,
    val reps: Int,
    val setType: String,
    val isCompleted: Boolean,
    val sessionStartTime: Long,
    val sessionEndTime: Long?
)
