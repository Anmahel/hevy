package com.example.gymtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Home : Screen("home", "Entrenar", Icons.Default.FitnessCenter)
    data object Routines : Screen("routines", "Rutinas", Icons.Default.ViewAgenda)
    data object History : Screen("history", "Historial", Icons.Default.History)
    data object Exercises : Screen("exercises", "Ejercicios", Icons.AutoMirrored.Filled.FormatListBulleted)

    data object ActiveWorkout : Screen("active_workout/{workoutId}", "Entrenamiento Activo") {
        fun createRoute(workoutId: Long) = "active_workout/$workoutId"
    }

    data object EditRoutine : Screen("edit_routine/{routineId}", "Editor de Rutina") {
        fun createRoute(routineId: Long = 0L) = "edit_routine/$routineId"
    }

    data object ExerciseDetail : Screen("exercise_detail/{exerciseId}", "Detalle de Ejercicio") {
        fun createRoute(exerciseId: Long) = "exercise_detail/$exerciseId"
    }

    companion object {
        val bottomNavItems: List<Screen> get() = listOf(Routines, History, Exercises)
    }
}
