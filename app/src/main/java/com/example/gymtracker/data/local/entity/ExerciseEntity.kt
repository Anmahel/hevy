package com.example.gymtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val muscleGroup: String,
    val equipment: String,
    val isCustom: Boolean = false,
    val defaultRestSeconds: Int = 90,
    val gifPathOrUrl: String = "",
    val thumbnailUrl: String = "",
    val primaryMuscle: String = "",
    val secondaryMuscles: String = "",
    val instructions: String = "",
    val pinnedNote: String = ""
)
