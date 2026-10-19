package com.example.gymtracker.domain.util

import kotlin.math.roundToInt

/**
 * Modelo de datos con el desglose exacto de macronutrientes y calorías
 * calculados para hipertrofia muscular y volumen limpio.
 */
data class MacroBreakdown(
    val weightKg: Float,
    val maintenanceCalories: Int,
    val surplusCalories: Int = 300,
    val totalCalories: Int,
    val proteinMinGrams: Float,
    val proteinMaxGrams: Float,
    val proteinTargetGrams: Float,
    val proteinCalories: Int,
    val fatsGrams: Float,
    val fatsCalories: Int,
    val carbsGrams: Float,
    val carbsCalories: Int,
    val proteinPercentage: Float,
    val carbsPercentage: Float,
    val fatsPercentage: Float
)

/**
 * Calculadora nutricional basada en evidencia científica para hipertrofia muscular.
 *
 * Fórmulas:
 * 1. Proteína objetivo: 2.0g a 2.2g por kg de peso corporal.
 * 2. Calorías diarias para superávit controlado:
 *    Base de mantenimiento (~32 kcal/kg) + superávit de hipertrofia (+300 kcal).
 * 3. Grasas saludables: 0.9g por kg.
 * 4. Carbohidratos: El resto de las calorías dividido entre 4:
 *    ((Calorías totales - (Proteína*4 + Grasas*9)) / 4).
 */
object MacroCalculator {

    const val MAINTENANCE_KCAL_PER_KG = 32f
    const val HYPERTROPHY_SURPLUS_KCAL = 300
    const val PROTEIN_MIN_PER_KG = 2.0f
    const val PROTEIN_MAX_PER_KG = 2.2f
    const val PROTEIN_TARGET_PER_KG = 2.0f
    const val FATS_PER_KG = 0.9f

    fun calculate(weightKg: Float): MacroBreakdown {
        val safeWeight = weightKg.coerceAtLeast(30f)

        // 1. Calorías: Mantenimiento + Superávit hipertrofia (+300 kcal)
        val maintenanceCalories = (safeWeight * MAINTENANCE_KCAL_PER_KG).roundToInt()
        val totalCalories = maintenanceCalories + HYPERTROPHY_SURPLUS_KCAL

        // 2. Proteína: 2.0g a 2.2g por kg
        val proteinMinGrams = (safeWeight * PROTEIN_MIN_PER_KG * 10f).roundToInt() / 10f
        val proteinMaxGrams = (safeWeight * PROTEIN_MAX_PER_KG * 10f).roundToInt() / 10f
        val proteinTargetGrams = (safeWeight * PROTEIN_TARGET_PER_KG * 10f).roundToInt() / 10f
        val proteinCalories = (proteinTargetGrams * 4f).roundToInt()

        // 3. Grasas: 0.9g por kg
        val fatsGrams = (safeWeight * FATS_PER_KG * 10f).roundToInt() / 10f
        val fatsCalories = (fatsGrams * 9f).roundToInt()

        // 4. Carbohidratos: El resto calórico dividido entre 4 kcal/g
        val remainingCalories = (totalCalories - (proteinCalories + fatsCalories)).coerceAtLeast(0)
        val carbsGrams = ((remainingCalories / 4f) * 10f).roundToInt() / 10f
        val carbsCalories = (carbsGrams * 4f).roundToInt()

        // Porcentajes para barra visual
        val totalMacroCal = (proteinCalories + fatsCalories + carbsCalories).coerceAtLeast(1)
        val proteinPct = proteinCalories.toFloat() / totalMacroCal
        val carbsPct = carbsCalories.toFloat() / totalMacroCal
        val fatsPct = fatsCalories.toFloat() / totalMacroCal

        return MacroBreakdown(
            weightKg = safeWeight,
            maintenanceCalories = maintenanceCalories,
            surplusCalories = HYPERTROPHY_SURPLUS_KCAL,
            totalCalories = totalCalories,
            proteinMinGrams = proteinMinGrams,
            proteinMaxGrams = proteinMaxGrams,
            proteinTargetGrams = proteinTargetGrams,
            proteinCalories = proteinCalories,
            fatsGrams = fatsGrams,
            fatsCalories = fatsCalories,
            carbsGrams = carbsGrams,
            carbsCalories = carbsCalories,
            proteinPercentage = proteinPct,
            carbsPercentage = carbsPct,
            fatsPercentage = fatsPct
        )
    }
}
