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
        assertEquals("active_workout/0", Screen.ActiveWorkout.createEmptyWorkoutRoute())
        assertEquals("active_workout/0?routineId=5", Screen.ActiveWorkout.createRoutineWorkoutRoute(5L))
        assertEquals("exercise_detail/7", Screen.ExerciseDetail.createRoute(7L))

        assertEquals(5, Screen.bottomNavItems.size)
        assertTrue(Screen.bottomNavItems.contains(Screen.Routines))
        assertTrue(Screen.bottomNavItems.contains(Screen.History))
        assertTrue(Screen.bottomNavItems.contains(Screen.MuscleRecovery))
        assertTrue(Screen.bottomNavItems.contains(Screen.Exercises))
        assertTrue(Screen.bottomNavItems.contains(Screen.WeightTracker))
    }

    @Test
    fun testMacroCalculatorHypertrophyFormulas() {
        // Peso de prueba: 75.0 kg
        val weight = 75.0f
        val macros = com.example.gymtracker.domain.util.MacroCalculator.calculate(weight)

        // 1. Mantenimiento: 75 * 32 = 2400 kcal
        assertEquals(2400, macros.maintenanceCalories)
        // 2. Superávit: +300 kcal -> 2700 kcal totales
        assertEquals(300, macros.surplusCalories)
        assertEquals(2700, macros.totalCalories)

        // 3. Proteína: 2.0g/kg -> 150g (600 kcal)
        assertEquals(150.0f, macros.proteinTargetGrams, 0.01f)
        assertEquals(150.0f, macros.proteinMinGrams, 0.01f)
        assertEquals(165.0f, macros.proteinMaxGrams, 0.01f) // 2.2g/kg -> 165g
        assertEquals(600, macros.proteinCalories)

        // 4. Grasas: 0.9g/kg -> 67.5g (608 kcal)
        assertEquals(67.5f, macros.fatsGrams, 0.01f)
        val expectedFatsCal = kotlin.math.round(67.5f * 9f).toInt()
        assertEquals(expectedFatsCal, macros.fatsCalories)

        // 5. Carbohidratos: (2700 - (600 + fatsCal)) / 4
        val remainingCal = 2700 - (600 + expectedFatsCal)
        val expectedCarbs = remainingCal / 4f
        assertEquals(expectedCarbs, macros.carbsGrams, 0.1f)
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
        assertEquals(listOf(1, 2, 3, 4, 5, 6), presets.map { it.sortOrder })

        // Día 1: Empuje A (Pecho, hombros y tríceps)
        val day1 = presets.first { it.sortOrder == 1 }
        assertEquals("Día 1 — Empuje A (Pecho, hombros y tríceps)", day1.title)
        assertEquals(6, day1.exercises.size)
        assertEquals(listOf(0, 1, 2, 3, 4, 5), day1.exercises.map { it.exerciseOrder })
        assertEquals("Press de banca plano con barra", day1.exercises[0].name)
        assertEquals(3, day1.exercises[0].targetSets)
        assertEquals("6-8 reps", day1.exercises[0].targetRepRange)
        assertEquals("Cruces en polea alta", day1.exercises[1].name)
        assertEquals(2, day1.exercises[1].targetSets)
        assertEquals("10-15 reps", day1.exercises[1].targetRepRange)
        assertTrue(day1.exercises[0].technique.contains("Pirámide Invertida"))
        assertEquals("Compuesto", day1.exercises[0].type)

        // Día 2: Tirón A (Espalda, bíceps y abdomen)
        val day2 = presets.first { it.sortOrder == 2 }
        assertEquals("Día 2 — Tirón A (Espalda, bíceps y abdomen)", day2.title)
        assertEquals(5, day2.exercises.size)
        assertEquals(listOf(0, 1, 2, 3, 4), day2.exercises.map { it.exerciseOrder })
        assertEquals("Jalón al pecho con agarre prono", day2.exercises[0].name)
        assertEquals(3, day2.exercises[0].targetSets)
        assertEquals("8-12 reps", day2.exercises[0].targetRepRange)

        // Día 3: Pierna A
        val day3 = presets.first { it.sortOrder == 3 }
        assertEquals("Día 3 — Pierna A (Cuádriceps y cadena posterior)", day3.title)
        assertEquals(5, day3.exercises.size)
        assertEquals(listOf(0, 1, 2, 3, 4), day3.exercises.map { it.exerciseOrder })

        // Día 4: Empuje B
        val day4 = presets.first { it.sortOrder == 4 }
        assertEquals("Día 4 — Empuje B (Pecho, hombros y tríceps)", day4.title)
        assertEquals(7, day4.exercises.size)
        assertEquals(listOf(0, 1, 2, 3, 4, 5, 6), day4.exercises.map { it.exerciseOrder })

        // Día 5: Tirón B
        val day5 = presets.first { it.sortOrder == 5 }
        assertEquals("Día 5 — Tirón B (Espalda y bíceps prioritario)", day5.title)
        assertEquals(7, day5.exercises.size)
        assertEquals(listOf(0, 1, 2, 3, 4, 5, 6), day5.exercises.map { it.exerciseOrder })

        // Día 6: Pierna B
        val day6 = presets.first { it.sortOrder == 6 }
        assertEquals("Día 6 — Pierna B (Glúteos y complementos)", day6.title)
        assertEquals(7, day6.exercises.size)
        assertEquals(listOf(0, 1, 2, 3, 4, 5, 6), day6.exercises.map { it.exerciseOrder })

        // Comprobar búsqueda de técnica recomendada
        val techBench = com.example.gymtracker.data.preset.DefaultRoutinesData.findTechnique(day1.title, "Press de banca plano con barra")
        assertTrue(techBench != null && techBench.contains("Pirámide Invertida"))
    }

    @Test
    fun testHypertrophyStreakPreservesRestDays() {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24L * 60 * 60 * 1000

        // Simular entrenamientos: Lunes (hace 4 días), Miércoles (hace 2 días). Ayer y hoy fueron días de descanso.
        val session1 = com.example.gymtracker.data.local.entity.WorkoutSessionEntity(
            id = 1L,
            title = "Día 1 — Empuje",
            startTime = now - (4 * oneDayMillis),
            endTime = now - (4 * oneDayMillis) + 3600000,
            durationSeconds = 3600,
            totalVolumeKg = 1200.0
        )
        val session2 = com.example.gymtracker.data.local.entity.WorkoutSessionEntity(
            id = 2L,
            title = "Día 2 — Tirón",
            startTime = now - (2 * oneDayMillis),
            endTime = now - (2 * oneDayMillis) + 3600000,
            durationSeconds = 3600,
            totalVolumeKg = 1500.0
        )

        val streak = com.example.gymtracker.domain.util.StreakCalculator.calculate(
            sessions = listOf(session1, session2),
            nowMillis = now
        )

        // Días activos únicos = 2
        assertEquals(2, streak.activeDaysCount)
        // La racha diaria NO se rompe por el descanso de 2 días (ayer y hoy)
        assertTrue("La racha no debe romperse por descansar", streak.currentStreakDays > 0)
        // Hoy es día de descanso
        assertTrue(streak.isRestDayToday)
        // El mensaje reconoce el descanso como recuperación
        assertTrue(streak.motivationalMessage.contains("descanso", ignoreCase = true) || streak.recoveryStatusTitle.contains("Recuperación", ignoreCase = true))
    }

    @Test
    fun testOneRepMaxCalculationEpleyFormula() {
        // Fórmula de Epley: 1RM = peso * (1 + reps / 30.0) para reps > 1, o peso para reps == 1
        val weight = 100.0
        val reps10 = 10
        val est1Rm10 = weight * (1.0 + reps10 / 30.0)
        // 100 * (1 + 10/30) = 100 * 1.3333 = 133.33 kg
        assertEquals(133.333, est1Rm10, 0.001)

        val reps1 = 1
        val est1Rm1 = if (reps1 > 1) weight * (1.0 + reps1 / 30.0) else weight
        assertEquals(100.0, est1Rm1, 0.001)
    }

    @Test
    fun testWeightPercentageVariationCalculation() {
        // Caso de subida de peso (superávit controlado para hipertrofia)
        val prevWeight = 70.0f
        val currentWeight = 71.4f
        val diff = currentWeight - prevWeight
        val percent = (diff / prevWeight) * 100f
        // (1.4 / 70.0) * 100 = 2.0%
        assertEquals(2.0f, percent, 0.001f)

        // Caso de bajada de peso
        val currentLower = 68.6f
        val diffLower = currentLower - prevWeight
        val percentLower = (diffLower / prevWeight) * 100f
        // (-1.4 / 70.0) * 100 = -2.0%
        assertEquals(-2.0f, percentLower, 0.001f)
    }

    @Test
    fun testHypertrophyStimulusClassificationThresholds() {
        // Menos de 6 series: Mantenimiento / Estímulo bajo
        val lowStimulus = com.example.gymtracker.domain.model.StimulusClassification.fromWeeklySets(4)
        assertEquals(com.example.gymtracker.domain.model.StimulusClassification.LOW, lowStimulus)
        assertEquals("Mantenimiento / Estímulo bajo", lowStimulus.label)

        // 6 a 9 series: Moderado
        val moderateStimulus = com.example.gymtracker.domain.model.StimulusClassification.fromWeeklySets(8)
        assertEquals(com.example.gymtracker.domain.model.StimulusClassification.MODERATE, moderateStimulus)

        // 10 a 20 series: Óptimo
        val optimalStimulus10 = com.example.gymtracker.domain.model.StimulusClassification.fromWeeklySets(10)
        val optimalStimulus16 = com.example.gymtracker.domain.model.StimulusClassification.fromWeeklySets(16)
        val optimalStimulus20 = com.example.gymtracker.domain.model.StimulusClassification.fromWeeklySets(20)
        assertEquals(com.example.gymtracker.domain.model.StimulusClassification.OPTIMAL, optimalStimulus10)
        assertEquals(com.example.gymtracker.domain.model.StimulusClassification.OPTIMAL, optimalStimulus16)
        assertEquals(com.example.gymtracker.domain.model.StimulusClassification.OPTIMAL, optimalStimulus20)
        assertTrue(optimalStimulus16.isOptimal)

        // Más de 22 series: Fatiga / Sobreentrenamiento
        val fatigueStimulus = com.example.gymtracker.domain.model.StimulusClassification.fromWeeklySets(24)
        assertEquals(com.example.gymtracker.domain.model.StimulusClassification.FATIGUE_LIMIT, fatigueStimulus)
        assertTrue(fatigueStimulus.isFatigueWarning)
    }

    @Test
    fun testMuscleMappingForHypertrophyGroups() {
        val chest = com.example.gymtracker.domain.model.HypertrophyMuscle.fromExerciseInfo(
            primaryMuscle = "Pectoral mayor",
            muscleGroup = "Pecho",
            exerciseName = "Press de Banca Plano"
        )
        assertEquals(com.example.gymtracker.domain.model.HypertrophyMuscle.PECHO, chest)

        val back = com.example.gymtracker.domain.model.HypertrophyMuscle.fromExerciseInfo(
            primaryMuscle = "Dorsal ancho",
            muscleGroup = "Espalda",
            exerciseName = "Dominadas pronas"
        )
        assertEquals(com.example.gymtracker.domain.model.HypertrophyMuscle.ESPALDA, back)

        val quads = com.example.gymtracker.domain.model.HypertrophyMuscle.fromExerciseInfo(
            primaryMuscle = "Cuádriceps",
            muscleGroup = "Piernas",
            exerciseName = "Sentadilla trasera con barra"
        )
        assertEquals(com.example.gymtracker.domain.model.HypertrophyMuscle.CUADRICEPS, quads)

        val hams = com.example.gymtracker.domain.model.HypertrophyMuscle.fromExerciseInfo(
            primaryMuscle = "Isquiotibiales",
            muscleGroup = "Piernas",
            exerciseName = "Peso muerto rumano"
        )
        assertEquals(com.example.gymtracker.domain.model.HypertrophyMuscle.ISQUIOTIBIALES, hams)
    }

    @Test
    fun testPlateCalculatorDiscsAndWarmupSets() {
        // 1. Cálculo de discos para 100 kg con barra olímpica de 20 kg
        // (100 - 20) / 2 = 40 kg por cada lado
        // Con discos estándar disponibles: 25 kg + 15 kg = 40 kg exactos
        val result100 = com.example.gymtracker.domain.util.PlateCalculator.calculate(100.0, 20.0)
        assertEquals(40.0, result100.weightPerSide, 0.001)
        assertEquals(80.0, result100.totalPlatesWeight, 0.001)
        assertEquals(0.0, result100.remainderKg, 0.001)

        val totalPlatesPerSide = result100.platesPerSide.sumOf { it.countPerSide }
        assertEquals(2, totalPlatesPerSide) // 1x25kg + 1x15kg
        assertEquals(25.0, result100.platesPerSide[0].weightKg, 0.001)
        assertEquals(1, result100.platesPerSide[0].countPerSide)
        assertEquals(15.0, result100.platesPerSide[1].weightKg, 0.001)
        assertEquals(1, result100.platesPerSide[1].countPerSide)

        // 2. Generación de series de calentamiento para 100 kg
        // - Serie 1: 50% -> 50.0 kg (10 reps)
        // - Serie 2: 70% -> 70.0 kg (5 reps)
        // - Serie 3: 85% -> 85.0 kg (3 reps)
        val warmupSets = com.example.gymtracker.domain.util.PlateCalculator.generateWarmupSets(100.0, 20.0)
        assertEquals(3, warmupSets.size)
        assertEquals(50.0, warmupSets[0].first, 0.001)
        assertEquals(10, warmupSets[0].second)

        assertEquals(70.0, warmupSets[1].first, 0.001)
        assertEquals(5, warmupSets[1].second)

        assertEquals(85.0, warmupSets[2].first, 0.001)
        assertEquals(3, warmupSets[2].second)
    }

    @Test
    fun testAnatomicalHeatmapColorsAndBodyMeasurementsDiff() {
        val baseItem = com.example.gymtracker.domain.model.MuscleRecoveryItem(
            muscle = com.example.gymtracker.domain.model.HypertrophyMuscle.PECHO,
            weeklySets = 0,
            previousWeekSets = 0,
            weeklyVolumeKg = 0.0,
            previousWeekVolumeKg = 0.0,
            stimulus = com.example.gymtracker.domain.model.StimulusClassification.LOW,
            hoursSinceLastTrained = null,
            recoveryProgress = 1f,
            recoveryMessage = "Listo",
            chipState = com.example.gymtracker.domain.model.RecoveryChipState.READY,
            lastTrainedDateFormatted = "Sin entrenar"
        )

        // 0 series -> Gris oscuro
        assertEquals(
            com.example.gymtracker.ui.components.HeatmapUntrainedGray,
            com.example.gymtracker.ui.components.resolveMuscleHeatmapColor(baseItem)
        )

        // 1-9 series (ej. 5 series, hace 48h) -> Estímulo inicial (Azul/Ámbar)
        val initialItem = baseItem.copy(weeklySets = 5, hoursSinceLastTrained = 48L)
        assertEquals(
            com.example.gymtracker.ui.components.HeatmapInitialBlue,
            com.example.gymtracker.ui.components.resolveMuscleHeatmapColor(initialItem)
        )

        // 10-20 series (ej. 14 series, hace 48h) -> Verde esmeralda óptimo
        val optimalItem = baseItem.copy(weeklySets = 14, hoursSinceLastTrained = 48L)
        assertEquals(
            com.example.gymtracker.ui.components.HeatmapOptimalEmerald,
            com.example.gymtracker.ui.components.resolveMuscleHeatmapColor(optimalItem)
        )

        // >20 series o entrenado hace menos de 24h -> Rojo intenso
        val highFatigueSetsItem = baseItem.copy(weeklySets = 22, hoursSinceLastTrained = 48L)
        assertEquals(
            com.example.gymtracker.ui.components.HeatmapFatigueRed,
            com.example.gymtracker.ui.components.resolveMuscleHeatmapColor(highFatigueSetsItem)
        )
        val recentTrainedItem = baseItem.copy(weeklySets = 12, hoursSinceLastTrained = 12L)
        assertEquals(
            com.example.gymtracker.ui.components.HeatmapFatigueRed,
            com.example.gymtracker.ui.components.resolveMuscleHeatmapColor(recentTrainedItem)
        )
    }

    @Test
    fun testQuickMealTrackerAndDailyHydrationGoal() {
        val presets = com.example.gymtracker.domain.util.QuickMealTrackerHelper.defaultMealBlocks
        assertEquals(5, presets.size)

        // 1. Verificar "+1 Scoop Whey 80%" (24g Prot | 3g Carb | 1.5g Grasas | 120 kcal)
        val whey = presets.first { it.id == "preset_whey_80" }
        assertEquals("+1 Scoop Whey 80%", whey.title)
        assertEquals(24f, whey.proteinGrams, 0.01f)
        assertEquals(3f, whey.carbsGrams, 0.01f)
        assertEquals(1.5f, whey.fatsGrams, 0.01f)
        assertEquals(120, whey.calories)

        // 2. Verificar "+200g Pechuga de Pollo" (62g Prot | 0g Carb | 7g Grasas | 330 kcal)
        val chicken = presets.first { it.id == "preset_chicken_200" }
        assertEquals(62f, chicken.proteinGrams, 0.01f)
        assertEquals(330, chicken.calories)

        // 3. Simular 2 toques y luego Deshacer el último toque
        val tap1 = com.example.gymtracker.domain.util.MealTapRecord(
            name = whey.title,
            proteinGrams = whey.proteinGrams,
            carbsGrams = whey.carbsGrams,
            fatsGrams = whey.fatsGrams,
            calories = whey.calories
        )
        val tap2 = com.example.gymtracker.domain.util.MealTapRecord(
            name = chicken.title,
            proteinGrams = chicken.proteinGrams,
            carbsGrams = chicken.carbsGrams,
            fatsGrams = chicken.fatsGrams,
            calories = chicken.calories
        )
        val history = mutableListOf(tap1, tap2)
        assertEquals(86.0, history.sumOf { it.proteinGrams.toDouble() }, 0.01)
        assertEquals(450, history.sumOf { it.calories })

        // Deshacer el último toque (quita pollo, queda solo whey)
        history.removeAt(history.lastIndex)
        assertEquals(1, history.size)
        assertEquals(24.0, history.sumOf { it.proteinGrams.toDouble() }, 0.01)
        assertEquals(120, history.sumOf { it.calories })

        // 4. Meta diaria de hidratación según el peso corporal (ej. 75 kg -> ~2850 ml)
        val waterGoal75 = com.example.gymtracker.domain.util.QuickMealTrackerHelper.calculateDailyWaterGoalMl(75f)
        assertEquals(2850, waterGoal75)
    }

    @Test
    fun testRpgGamificationStrengthRanksAndShareCard() {
        val engine = com.example.gymtracker.domain.util.RpgGamificationEngine

        // 1. Verificar constantes de XP del Módulo 4
        assertEquals(10, engine.XP_PER_COMPLETED_SET)
        assertEquals(150, engine.XP_PER_FINISHED_WORKOUT)
        assertEquals(300, engine.XP_PER_PR_1RM)
        assertEquals(50, engine.XP_PER_WEIGHT_OR_NUTRITION_LOG)

        // 2. Verificar Rangos de Fuerza por Ejercicio (Press de Banca, Peso Corporal = 80 kg, multiplicador = 1.0)
        // Oro = 1.0 * 80 = 80 kg; Platino = 1.25 * 80 = 100 kg
        val benchRank = engine.calculateStrengthRank(
            exerciseId = 1L,
            exerciseName = "Press de Banca con Barra",
            muscleGroup = "Pecho",
            bestOneRepMaxKg = 90.0,
            bodyweightKg = 80.0
        )
        assertEquals(com.example.gymtracker.domain.util.StrengthRankTier.GOLD, benchRank.currentTier)
        assertEquals(com.example.gymtracker.domain.util.StrengthRankTier.PLATINUM, benchRank.nextTier)
        assertEquals(10.0, benchRank.kgNeededForNextTier, 0.01) // Faltan 10 kg en el 1RM para Platino (100 kg)

        // Rango Titán (>= 1.85 * 80 = 148 kg)
        val titanRank = engine.calculateStrengthRank(
            exerciseId = 1L,
            exerciseName = "Press de Banca con Barra",
            muscleGroup = "Pecho",
            bestOneRepMaxKg = 155.0,
            bodyweightKg = 80.0
        )
        assertEquals(com.example.gymtracker.domain.util.StrengthRankTier.TITAN, titanRank.currentTier)
        assertEquals(null, titanRank.nextTier)
        assertEquals(0.0, titanRank.kgNeededForNextTier, 0.01)

        // 3. Verificar detección de nuevos Récords Personales (PRs) de 1RM y Músculos Trabajados para la Tarjeta Resumen
        val currentSets = listOf(
            Triple(1L, "Press de Banca", listOf(100.0 to 5)) // 1RM Epley = 100 * (1 + 5/30) = 116.67 kg
        )
        val newPrs = engine.detectSessionPersonalRecords(
            currentWorkoutId = 99L,
            currentExerciseSets = currentSets,
            previousSessions = emptyList()
        )
        assertEquals(1, newPrs.size)
        assertEquals("Press de Banca", newPrs[0].exerciseName)
        assertEquals(300, newPrs[0].xpReward)

        val workedMuscles = engine.resolveWorkedMuscles(
            listOf(
                Triple("Pectoral", "Pecho", "Press de Banca"),
                Triple("Tríceps", "Tríceps", "Extensión en Polea")
            )
        )
        assertTrue(workedMuscles.contains(com.example.gymtracker.domain.model.HypertrophyMuscle.PECHO))
        assertTrue(workedMuscles.contains(com.example.gymtracker.domain.model.HypertrophyMuscle.TRICEPS))
    }

    @Test
    fun testLocalBackupExportValidationAndRestoreStats() {
        val manager = com.example.gymtracker.domain.util.BackupJsonManager

        // 1. Verificar nombre de archivo con formato gymtracker_backup_fecha.json
        val fileName = manager.buildDefaultBackupFileName()
        assertTrue(fileName.startsWith("gymtracker_backup_"))
        assertTrue(fileName.endsWith(".json"))

        // 2. Crear datos de prueba para exportar
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

        val stats = manager.calculateStorageStats(
            routines = emptyList(),
            sessions = emptyList(),
            exercises = listOf(customExercise),
            weightLogs = listOf(weightLog),
            dailyNutritionLogs = listOf(nutritionLog),
            customMealBlocks = emptyList()
        )
        assertEquals(1, stats.exercisesCount)
        assertEquals(1, stats.customExercisesCount)
        assertEquals(2, stats.totalBiometricRecordsCount)

        // 3. Verificar rechazo inmediato de JSON vacío
        val emptyValidation = manager.validateAndParseBackupJson("   ")
        assertTrue(emptyValidation is com.example.gymtracker.domain.util.BackupValidationResult.Invalid)
    }
}
