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
            val clean = value.trim()
            return entries.firstOrNull {
                it.name.equals(clean, ignoreCase = true) ||
                it.displayName.equals(clean, ignoreCase = true)
            } ?: when {
                clean.contains("Pectoral", ignoreCase = true) || clean.contains("Pecho", ignoreCase = true) -> CHEST
                clean.contains("Dorsal", ignoreCase = true) || clean.contains("Espalda", ignoreCase = true) || clean.contains("Romboid", ignoreCase = true) -> BACK
                clean.contains("Pierna", ignoreCase = true) || clean.contains("Cuádricep", ignoreCase = true) || clean.contains("Isquio", ignoreCase = true) || clean.contains("Femoral", ignoreCase = true) || clean.contains("Glúteo", ignoreCase = true) || clean.contains("Gemelo", ignoreCase = true) || clean.contains("Pantorrilla", ignoreCase = true) -> LEGS
                clean.contains("Hombro", ignoreCase = true) || clean.contains("Deltoide", ignoreCase = true) -> SHOULDERS
                clean.contains("Brazo", ignoreCase = true) || clean.contains("Bícep", ignoreCase = true) || clean.contains("Trícep", ignoreCase = true) || clean.contains("Braquial", ignoreCase = true) || clean.contains("Antebrazo", ignoreCase = true) -> ARMS
                clean.contains("Core", ignoreCase = true) || clean.contains("Abdomen", ignoreCase = true) || clean.contains("Abdominal", ignoreCase = true) -> CORE
                clean.contains("Cardio", ignoreCase = true) -> CARDIO
                else -> CHEST
            }
        }
    }
}
