package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Gym Tracker", appName)
  }

  @Test
  fun `export and validate full backup json`() {
    val manager = com.example.gymtracker.domain.util.BackupJsonManager
    val customExercise = com.example.gymtracker.data.local.entity.ExerciseEntity(
      id = 500L,
      name = "Press Inclinado Convergente",
      muscleGroup = "Pecho",
      equipment = "Máquina",
      isCustom = true,
      pinnedNote = "Asiento en posición 3"
    )
    val weightLog = com.example.gymtracker.data.local.entity.WeightLogEntity(
      id = 10L,
      weightKg = 78.5f,
      date = 1710000000000L,
      notes = "En ayunas",
      armCm = 40.5f,
      waistCm = 79.0f
    )
    val nutritionLog = com.example.gymtracker.data.local.entity.DailyNutritionLogEntity(
      dateKey = "2026-10-17",
      consumedCalories = 2850,
      consumedProteinGrams = 175f,
      consumedCarbsGrams = 340f,
      consumedFatsGrams = 75f,
      creatineTaken = true,
      waterMl = 3000
    )

    val exportedJson = manager.exportFullBackupToJson(
      routines = emptyList(),
      sessions = emptyList(),
      exercises = listOf(customExercise),
      weightLogs = listOf(weightLog),
      dailyNutritionLogs = listOf(nutritionLog),
      customMealBlocks = emptyList()
    )

    val validationResult = manager.validateAndParseBackupJson(exportedJson)
    org.junit.Assert.assertTrue(validationResult is com.example.gymtracker.domain.util.BackupValidationResult.Valid)
    val validPayload = (validationResult as com.example.gymtracker.domain.util.BackupValidationResult.Valid).payload
    assertEquals(1, validPayload.exercises.size)
    assertEquals("Press Inclinado Convergente", validPayload.exercises[0].name)
    assertEquals(1, validPayload.weightLogs.size)
    assertEquals(78.5f, validPayload.weightLogs[0].weightKg, 0.01f)
    assertEquals(40.5f, validPayload.weightLogs[0].armCm ?: 0f, 0.01f)
    assertEquals(1, validPayload.dailyNutritionLogs.size)
    org.junit.Assert.assertTrue(validPayload.dailyNutritionLogs[0].creatineTaken)

    val corruptedValidation = manager.validateAndParseBackupJson("{ json_corrupto: [ ")
    org.junit.Assert.assertTrue(corruptedValidation is com.example.gymtracker.domain.util.BackupValidationResult.Invalid)
  }

  @Test
  fun `persist and recover active workout session state in real time`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val store = com.example.gymtracker.data.local.ActiveWorkoutSessionStore
    store.init(context)
    store.clearState(context)

    val startTs = System.currentTimeMillis() - 120_000L
    val restStartTs = System.currentTimeMillis() - 30_000L
    val sampleWorkout = com.example.gymtracker.domain.model.Workout(
      id = 42L,
      title = "Día 1 — Empuje (Pecho, Hombro y Tríceps)",
      startTimeMillis = startTs
    )
    val sampleSet = com.example.gymtracker.domain.model.WorkoutSet(
      id = 7L,
      workoutId = 42L,
      exerciseId = 101L,
      exerciseName = "Press de banca plano con barra",
      setNumber = 1,
      weightKg = 85.0,
      reps = 8,
      isCompleted = true,
      exerciseOrder = 0,
      targetRepRange = "6-8 reps"
    )
    val workoutWithSets = com.example.gymtracker.domain.model.WorkoutWithSets(
      workout = sampleWorkout,
      exercises = listOf(
        com.example.gymtracker.domain.model.ExerciseSetsGroup(
          exercise = com.example.gymtracker.domain.model.Exercise(
            id = 101L,
            name = "Press de banca plano con barra",
            muscleGroup = com.example.gymtracker.domain.model.MuscleGroup.CHEST
          ),
          sets = listOf(sampleSet),
          targetRepRange = "6-8 reps"
        )
      )
    )

    store.saveState(
      workoutId = 42L,
      routineId = 1L,
      title = sampleWorkout.title,
      startTimestamp = startTs,
      restTimerActive = true,
      restTimerExpanded = false,
      restTimerStartTimestamp = restStartTs,
      restTimerTotalSeconds = 90,
      supersetPairs = mapOf(101L to 102L, 102L to 101L),
      focusedExerciseId = 102L,
      workoutWithSets = workoutWithSets,
      context = context
    )

    val loaded = store.loadState(context)
    org.junit.Assert.assertNotNull(loaded)
    assertEquals(42L, loaded!!.workoutId)
    assertEquals(1L, loaded.routineId)
    assertEquals(startTs, loaded.startTimestamp)
    org.junit.Assert.assertTrue(loaded.restTimerActive)
    assertEquals(restStartTs, loaded.restTimerStartTimestamp)
    assertEquals(1, loaded.setsSnapshot.size)
    assertEquals(85.0, loaded.setsSnapshot[0].weightKg, 0.01)
    assertEquals(8, loaded.setsSnapshot[0].reps)
    org.junit.Assert.assertTrue(loaded.setsSnapshot[0].isCompleted)

    store.clearState(context)
    org.junit.Assert.assertNull(store.loadState(context))
  }
}
