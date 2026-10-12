package com.example.gymtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long? = null,
    val notes: String = "",
    val totalVolumeKg: Double = 0.0,
    val totalSets: Int = 0
)
