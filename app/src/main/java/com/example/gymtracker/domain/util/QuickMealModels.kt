package com.example.gymtracker.domain.util

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

/**
 * Representa un bloque de comida rápida (1-Tap Meal Tracker), ya sea predeterminado o personalizado.
 */
data class QuickMealBlock(
    val id: String,
    val title: String,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatsGrams: Float,
    val calories: Int,
    val isCustom: Boolean = false,
    val customEntityId: Long? = null
)

/**
 * Representa un toque individual registrado durante el día para permitir "Deshacer el último toque".
 */
data class MealTapRecord(
    val name: String,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatsGrams: Float,
    val calories: Int,
    val timestamp: Long = System.currentTimeMillis()
)

object QuickMealTrackerHelper {

    /**
     * Los 5 bloques rápidos oficiales de comidas habituales para hipertrofia:
     * - "+1 Scoop Whey 80%" (24g Prot | 3g Carb | 1.5g Grasas | 120 kcal)
     * - "+200g Pechuga de Pollo" (62g Prot | 0g Carb | 7g Grasas | 330 kcal)
     * - "+150g Carne Magra" (40g Prot | 0g Carb | 8g Grasas | 240 kcal)
     * - "+3 Huevos Enteros" (19g Prot | 1.5g Carb | 15g Grasas | 215 kcal)
     * - "+Bowl Yogur + Avena + Chía" (22g Prot | 45g Carb | 9g Grasas | 350 kcal)
     */
    val defaultMealBlocks: List<QuickMealBlock> = listOf(
        QuickMealBlock(
            id = "preset_whey_80",
            title = "+1 Scoop Whey 80%",
            proteinGrams = 24f,
            carbsGrams = 3f,
            fatsGrams = 1.5f,
            calories = 120,
            isCustom = false
        ),
        QuickMealBlock(
            id = "preset_chicken_200",
            title = "+200g Pechuga de Pollo",
            proteinGrams = 62f,
            carbsGrams = 0f,
            fatsGrams = 7f,
            calories = 330,
            isCustom = false
        ),
        QuickMealBlock(
            id = "preset_beef_150",
            title = "+150g Carne Magra",
            proteinGrams = 40f,
            carbsGrams = 0f,
            fatsGrams = 8f,
            calories = 240,
            isCustom = false
        ),
        QuickMealBlock(
            id = "preset_eggs_3",
            title = "+3 Huevos Enteros",
            proteinGrams = 19f,
            carbsGrams = 1.5f,
            fatsGrams = 15f,
            calories = 215,
            isCustom = false
        ),
        QuickMealBlock(
            id = "preset_bowl_yogurt_oats",
            title = "+Bowl Yogur + Avena + Chía",
            proteinGrams = 22f,
            carbsGrams = 45f,
            fatsGrams = 9f,
            calories = 350,
            isCustom = false
        )
    )

    /**
     * Calcula la meta diaria recomendada de hidratación en mililitros según el peso corporal.
     * Estándar deportivo de hipertrofia y suplementación con creatina: ~38 ml por kg de peso
     * redondeado a múltiplos de 50 ml (mínimo 2250 ml).
     */
    fun calculateDailyWaterGoalMl(weightKg: Float): Int {
        val safeWeight = if (weightKg > 20f) weightKg else 75f
        val rawMl = safeWeight * 38f
        val rounded50 = ((rawMl / 50f).roundToInt() * 50)
        return rounded50.coerceIn(2000, 5500)
    }

    fun parseTapHistory(jsonStr: String?): List<MealTapRecord> {
        if (jsonStr.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<MealTapRecord>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    MealTapRecord(
                        name = obj.optString("name", "Comida"),
                        proteinGrams = obj.optDouble("proteinGrams", 0.0).toFloat(),
                        carbsGrams = obj.optDouble("carbsGrams", 0.0).toFloat(),
                        fatsGrams = obj.optDouble("fatsGrams", 0.0).toFloat(),
                        calories = obj.optInt("calories", 0),
                        timestamp = obj.optLong("timestamp", 0L)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun encodeTapHistory(records: List<MealTapRecord>): String {
        val array = JSONArray()
        records.forEach { rec ->
            val obj = JSONObject().apply {
                put("name", rec.name)
                put("proteinGrams", rec.proteinGrams.toDouble())
                put("carbsGrams", rec.carbsGrams.toDouble())
                put("fatsGrams", rec.fatsGrams.toDouble())
                put("calories", rec.calories)
                put("timestamp", rec.timestamp)
            }
            array.put(obj)
        }
        return array.toString()
    }
}
