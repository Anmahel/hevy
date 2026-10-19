package com.example.gymtracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sets",
    indices = [
        Index(value = ["workoutSessionId"]),
        Index(value = ["exerciseId"])
    ]
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutSessionId: Long,
    val exerciseId: Long,
    val setNumber: Int,
    val setType: String = "Normal", // Normal, Warmup, Drop, Failure
    val weightKg: Double,
    val reps: Int,
    val isCompleted: Boolean,
    val rpe: Double? = null,
    val exerciseOrder: Int = 0,
    val targetRepRange: String = ""
)
