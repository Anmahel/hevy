package com.example.gymtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Home : Screen("home", "Entrenar", Icons.Default.FitnessCenter)
    data object Routines : Screen("routines", "Rutinas", Icons.Default.ViewAgenda)
    data object History : Screen("history", "Historial", Icons.Default.History)
    data object Exercises : Screen("exercises", "Ejercicios", Icons.AutoMirrored.Filled.FormatListBulleted)
    data object WeightTracker : Screen("weight_tracker", "Nutrición", Icons.Default.Scale)
    data object Calendar : Screen("calendar", "Almanaque", Icons.Default.CalendarMonth)
    data object MuscleRecovery : Screen("muscle_recovery", "Estímulo", Icons.Default.FitnessCenter)
    data object RpgProfile : Screen("rpg_profile", "Perfil RPG de Atleta")
    data object PerformanceStats : Screen("performance_stats", "Estadísticas de Rendimiento")

    data object ActiveWorkout : Screen("active_workout/{workoutId}?routineId={routineId}", "Entrenamiento Activo") {
        fun createRoute(workoutId: Long = 0L, routineId: Long = 0L): String {
            return if (routineId > 0L) {
                "active_workout/$workoutId?routineId=$routineId"
            } else {
                "active_workout/$workoutId"
            }
        }

        fun createEmptyWorkoutRoute(): String = "active_workout/0"
        fun createRoutineWorkoutRoute(routineId: Long): String = "active_workout/0?routineId=$routineId"
    }

    data object EditRoutine : Screen("edit_routine/{routineId}", "Editor de Rutina") {
        fun createRoute(routineId: Long = 0L) = "edit_routine/$routineId"
    }

    data object ExerciseDetail : Screen("exercise_detail/{exerciseId}", "Detalle de Ejercicio") {
        fun createRoute(exerciseId: Long) = "exercise_detail/$exerciseId"
    }

    companion object {
        val bottomNavItems: List<Screen> get() = listOf(Routines, History, MuscleRecovery, WeightTracker, Exercises)
    }
}
