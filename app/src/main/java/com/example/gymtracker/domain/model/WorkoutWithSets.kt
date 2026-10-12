package com.example.gymtracker.domain.model

data class ExerciseSetsGroup(
    val exercise: Exercise,
    val sets: List<WorkoutSet>
)

data class WorkoutWithSets(
    val workout: Workout,
    val exercises: List<ExerciseSetsGroup>
)
