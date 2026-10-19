package com.example.gymtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room para el registro diario de peso corporal, notas y los 8 perímetros antropométricos en cm:
 * - Bíceps flaco / relajado (bicepsRelaxedCm)
 * - Bíceps duro / contraído (bicepsContractedCm / armCm)
 * - Antebrazos (forearmCm)
 * - Pecho (chestCm)
 * - Cintura (waistCm)
 * - Nalga / Glúteos (glutesCm)
 * - Muslo (thighCm)
 * - Gemelos (calfCm)
 */
@Entity(tableName = "weight_logs")
data class WeightLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val date: Long,
    val weightKg: Float,
    val notes: String? = null,
    // Medidas antropométricas con cinta métrica (en cm)
    val bicepsRelaxedCm: Float? = null,
    val bicepsContractedCm: Float? = null,
    val forearmCm: Float? = null,
    val armCm: Float? = null,
    val chestCm: Float? = null,
    val waistCm: Float? = null,
    val glutesCm: Float? = null,
    val thighCm: Float? = null,
    val calfCm: Float? = null,
    // Datos para el Comparador Visual Antes vs. Después
    val photoUri: String? = null,
    val poseType: String? = null
) {
    val resolvedBicepsContractedCm: Float?
        get() = bicepsContractedCm ?: armCm
}
