package com.example.gymtracker.domain.model

data class ExerciseSetsGroup(
    val exercise: Exercise,
    val sets: List<WorkoutSet>,
    val progressiveOverloadSuggestion: String? = null,
    val supersetGroupId: String? = null,
    val supersetLabel: String? = null, // ej. "Superserie A1", "Superserie A2"
    val targetRepRange: String = ""
)

data class WorkoutWithSets(
    val workout: Workout,
    val exercises: List<ExerciseSetsGroup>
)
