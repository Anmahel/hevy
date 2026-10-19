package com.example.gymtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room para persistir el consumo diario de macronutrientes 1-Tap,
 * creatina (5g) e hidratación (ml) por fecha (yyyy-MM-dd).
 */
@Entity(tableName = "daily_nutrition_logs")
data class DailyNutritionLogEntity(
    @PrimaryKey
    val dateKey: String, // Formato "yyyy-MM-dd"
    val consumedCalories: Int = 0,
    val consumedProteinGrams: Float = 0f,
    val consumedCarbsGrams: Float = 0f,
    val consumedFatsGrams: Float = 0f,
    val creatineTaken: Boolean = false,
    val waterMl: Int = 0,
    val tapHistoryJson: String = "[]"
)

/**
 * Entidad de Room para bloques personalizados de comidas rápidas (1-Tap Meal Tracker).
 */
@Entity(tableName = "custom_meal_blocks")
data class CustomMealBlockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatsGrams: Float,
    val calories: Int
)
