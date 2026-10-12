package com.example.gymtracker.domain.model

data class Exercise(
    val id: Long = 0,
    val name: String,
    val muscleGroup: MuscleGroup,
    val equipment: String = "Barra",
    val defaultRestSeconds: Int = 90,
    val gifPathOrUrl: String = "",
    val thumbnailUrl: String = "",
    val primaryMuscle: String = "",
    val secondaryMuscles: String = "",
    val instructions: String = ""
)
