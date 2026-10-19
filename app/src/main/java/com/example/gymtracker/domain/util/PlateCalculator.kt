package com.example.gymtracker.domain.util

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

data class PlateCount(
    val weightKg: Double,
    val countPerSide: Int,
    val color: Color,
    val label: String
)

data class PlateCalculationResult(
    val targetWeightKg: Double,
    val barWeightKg: Double,
    val weightPerSide: Double,
    val totalPlatesWeight: Double,
    val platesPerSide: List<PlateCount>,
    val remainderKg: Double
)

object PlateCalculator {
    // Pesos estándar de competición y gimnasio comercial con código de colores olímpico:
    // 25kg (Rojo), 20kg (Azul), 15kg (Amarillo), 10kg (Verde), 5kg (Blanco/Gris), 2.5kg (Rojo pequeño/Negro), 1.25kg (Cromo/Gris oscuro)
    val AVAILABLE_PLATES = listOf(
        25.0 to Color(0xFFE53935),  // Rojo olímpico
        20.0 to Color(0xFF1E88E5),  // Azul olímpico
        15.0 to Color(0xFFFDD835),  // Amarillo olímpico
        10.0 to Color(0xFF43A047),  // Verde olímpico
        5.0 to Color(0xFFECEFF1),   // Blanco / Gris claro
        2.5 to Color(0xFF212121),   // Negro / Acento oscuro
        1.25 to Color(0xFF78909C)   // Gris plata
    )

    fun calculate(targetWeightKg: Double, barWeightKg: Double = 20.0): PlateCalculationResult {
        if (targetWeightKg <= barWeightKg) {
            return PlateCalculationResult(
                targetWeightKg = targetWeightKg,
                barWeightKg = barWeightKg,
                weightPerSide = 0.0,
                totalPlatesWeight = 0.0,
                platesPerSide = emptyList(),
                remainderKg = 0.0
            )
        }

        val requiredPerSide = (targetWeightKg - barWeightKg) / 2.0
        var remaining = requiredPerSide
        val resultPlates = mutableListOf<PlateCount>()

        for ((plateWeight, color) in AVAILABLE_PLATES) {
            if (remaining >= plateWeight - 0.001) {
                val count = (remaining / plateWeight).toInt()
                if (count > 0) {
                    val label = if (plateWeight % 1.0 == 0.0) "${plateWeight.toInt()} kg" else "$plateWeight kg"
                    resultPlates.add(
                        PlateCount(
                            weightKg = plateWeight,
                            countPerSide = count,
                            color = color,
                            label = label
                        )
                    )
                    remaining -= count * plateWeight
                }
            }
        }

        val totalPlatesWeight = resultPlates.sumOf { it.weightKg * it.countPerSide } * 2.0
        val remainder = (requiredPerSide - (totalPlatesWeight / 2.0)).coerceAtLeast(0.0)

        return PlateCalculationResult(
            targetWeightKg = targetWeightKg,
            barWeightKg = barWeightKg,
            weightPerSide = requiredPerSide,
            totalPlatesWeight = totalPlatesWeight,
            platesPerSide = resultPlates,
            remainderKg = remainder
        )
    }

    /**
     * Genera 3 series progresivas de calentamiento:
     * - Serie 1: 50% del peso de trabajo (10 reps)
     * - Serie 2: 70% del peso de trabajo (5 reps)
     * - Serie 3: 85% del peso de trabajo (3 reps)
     * Redondeado al múltiplo más cercano de 2.5 kg.
     */
    fun generateWarmupSets(workingWeightKg: Double, barWeightKg: Double = 20.0): List<Pair<Double, Int>> {
        val effectiveWorking = workingWeightKg.coerceAtLeast(barWeightKg)
        val percentages = listOf(
            0.50 to 10,
            0.70 to 5,
            0.85 to 3
        )

        return percentages.map { (pct, reps) ->
            val rawWeight = effectiveWorking * pct
            val rounded = (rawWeight / 2.5).roundToInt() * 2.5
            val finalWeight = rounded.coerceAtLeast(barWeightKg)
            Pair(finalWeight, reps)
        }
    }
}
