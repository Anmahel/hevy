package com.example.gymtracker.domain.model

enum class MuscleGroup(val displayName: String) {
    CHEST("Pecho"),
    BACK("Espalda"),
    LEGS("Piernas"),
    SHOULDERS("Hombros"),
    ARMS("Brazos"),
    CORE("Core / Abdomen"),
    CARDIO("Cardio");

    companion object {
        fun fromString(value: String): MuscleGroup {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: CHEST
        }
    }
}
