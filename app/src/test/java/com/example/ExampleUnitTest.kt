package com.example

import com.example.gymtracker.domain.model.WorkoutSet
import com.example.gymtracker.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testNavigationScreenRoutes() {
        assertEquals("routines", Screen.Routines.route)
        assertEquals("history", Screen.History.route)
        assertEquals("exercises", Screen.Exercises.route)
        assertEquals("edit_routine/42", Screen.EditRoutine.createRoute(42L))
        assertEquals("active_workout/101", Screen.ActiveWorkout.createRoute(101L))
        assertEquals("exercise_detail/7", Screen.ExerciseDetail.createRoute(7L))

        assertEquals(3, Screen.bottomNavItems.size)
        assertTrue(Screen.bottomNavItems.contains(Screen.Routines))
        assertTrue(Screen.bottomNavItems.contains(Screen.History))
        assertTrue(Screen.bottomNavItems.contains(Screen.Exercises))
    }

    @Test
    fun testWorkoutVolumeAndCompletedSetsCalculation() {
        val sets = listOf(
            WorkoutSet(
                id = 1L,
                workoutId = 10L,
                exerciseId = 1L,
                exerciseName = "Bench Press",
                setNumber = 1,
                weightKg = 80.0,
                reps = 10,
                isCompleted = true
            ),
            WorkoutSet(
                id = 2L,
                workoutId = 10L,
                exerciseId = 1L,
                exerciseName = "Bench Press",
                setNumber = 2,
                weightKg = 85.0,
                reps = 8,
                isCompleted = true
            ),
            WorkoutSet(
                id = 3L,
                workoutId = 10L,
                exerciseId = 1L,
                exerciseName = "Bench Press",
                setNumber = 3,
                weightKg = 90.0,
                reps = 6,
                isCompleted = false // Incomplete, should not count towards volume
            )
        )

        val completedSets = sets.filter { it.isCompleted }
        val totalVolume = completedSets.sumOf { it.weightKg * it.reps }

        // Set 1: 80 * 10 = 800. Set 2: 85 * 8 = 680. Total = 1480.0
        assertEquals(2, completedSets.size)
        assertEquals(1480.0, totalVolume, 0.001)
    }

    @Test
    fun testDefaultRoutinesSixDaysPresets() {
        val presets = com.example.gymtracker.data.preset.DefaultRoutinesData.allPresets
        assertEquals("Debe contener 6 rutinas (Día 1 a Día 6)", 6, presets.size)

        // Día 1: Empuje A (Pecho prioritario)
        val day1 = presets.first { it.dayNumber == 1 }
        assertEquals("Día 1 — Empuje A (Pecho prioritario)", day1.title)
        assertEquals(6, day1.exercises.size)
        assertEquals("Press de banca plano con barra", day1.exercises[0].name)
        assertTrue(day1.exercises[0].technique.contains("Pirámide Invertida"))
        assertEquals("Compuesto", day1.exercises[0].type)

        // Día 2: Tirón A (Espalda y bíceps)
        val day2 = presets.first { it.dayNumber == 2 }
        assertEquals("Día 2 — Tirón A (Espalda y bíceps)", day2.title)
        assertEquals(5, day2.exercises.size)

        // Día 3: Pierna A
        val day3 = presets.first { it.dayNumber == 3 }
        assertEquals("Día 3 — Pierna A (Cuádriceps y cadena posterior)", day3.title)
        assertEquals(5, day3.exercises.size)

        // Día 4: Empuje B
        val day4 = presets.first { it.dayNumber == 4 }
        assertEquals("Día 4 — Empuje B (Pecho, hombros y tríceps)", day4.title)
        assertEquals(7, day4.exercises.size)

        // Día 5: Tirón B
        val day5 = presets.first { it.dayNumber == 5 }
        assertEquals("Día 5 — Tirón B (Espalda y bíceps prioritario)", day5.title)
        assertEquals(7, day5.exercises.size)

        // Día 6: Pierna B
        val day6 = presets.first { it.dayNumber == 6 }
        assertEquals("Día 6 — Pierna B (Glúteos y complementos)", day6.title)
        assertEquals(7, day6.exercises.size)

        // Comprobar búsqueda de técnica recomendada
        val techBench = com.example.gymtracker.data.preset.DefaultRoutinesData.findTechnique(day1.title, "Press de banca plano con barra")
        assertTrue(techBench != null && techBench.contains("Pirámide Invertida"))
    }
}
