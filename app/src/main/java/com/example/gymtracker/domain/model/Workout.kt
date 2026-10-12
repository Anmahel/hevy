package com.example.gymtracker.domain.model

data class Workout(
    val id: Long = 0,
    val title: String,
    val startTimeMillis: Long = System.currentTimeMillis(),
    val endTimeMillis: Long? = null,
    val notes: String = "",
    val totalVolumeKg: Double = 0.0,
    val totalSets: Int = 0
) {
    val durationMinutes: Long
        get() {
            val end = endTimeMillis ?: System.currentTimeMillis()
            val diff = (end - startTimeMillis) / (1000 * 60)
            return if (diff < 0) 0 else diff
        }

    val isActive: Boolean
        get() = endTimeMillis == null
}
