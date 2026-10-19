package com.example.gymtracker.domain.util

import com.example.gymtracker.data.local.entity.CustomMealBlockEntity
import com.example.gymtracker.data.local.entity.DailyNutritionLogEntity
import com.example.gymtracker.data.local.entity.ExerciseEntity
import com.example.gymtracker.data.local.entity.RoutineEntity
import com.example.gymtracker.data.local.entity.RoutineExerciseEntity
import com.example.gymtracker.data.local.entity.WeightLogEntity
import com.example.gymtracker.data.local.entity.WorkoutSessionEntity
import com.example.gymtracker.data.local.entity.WorkoutSetEntity
import com.example.gymtracker.data.local.relation.RoutineWithOrderedExercises
import com.example.gymtracker.data.local.relation.WorkoutSessionWithDetails
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Estadísticas en tiempo real del almacenamiento local en Room.
 */
data class LocalStorageStats(
    val routinesCount: Int = 0,
    val savedSessionsCount: Int = 0,
    val totalSetsCount: Int = 0,
    val exercisesCount: Int = 0,
    val customExercisesCount: Int = 0,
    val biometricWeightLogsCount: Int = 0,
    val nutritionDaysCount: Int = 0,
    val customMealsCount: Int = 0,
    val currentStreakDays: Int = 0,
    val bestStreakDays: Int = 0,
    val totalPersonalRecords: Int = 0
) {
    val totalBiometricRecordsCount: Int
        get() = biometricWeightLogsCount + nutritionDaysCount
}

/**
 * Representa una rutina empaquetada junto con los IDs ordenados de sus ejercicios.
 */
data class BackupRoutineRecord(
    val routine: RoutineEntity,
    val exerciseIds: List<Long>
)

/**
 * Representa una sesión de entrenamiento empaquetada con todas sus series.
 */
data class BackupWorkoutSessionRecord(
    val session: WorkoutSessionEntity,
    val sets: List<WorkoutSetEntity>
)

/**
 * Carga útil completa de una copia de seguridad local (.json).
 */
data class BackupPayload(
    val formatVersion: Int = 1,
    val exportedAtMillis: Long = System.currentTimeMillis(),
    val exportedDateFormatted: String = "",
    val currentStreakDays: Int = 0,
    val bestStreakDays: Int = 0,
    val totalPersonalRecords: Int = 0,
    val exercises: List<ExerciseEntity> = emptyList(),
    val routines: List<BackupRoutineRecord> = emptyList(),
    val workoutSessions: List<BackupWorkoutSessionRecord> = emptyList(),
    val weightLogs: List<WeightLogEntity> = emptyList(),
    val dailyNutritionLogs: List<DailyNutritionLogEntity> = emptyList(),
    val customMealBlocks: List<CustomMealBlockEntity> = emptyList()
)

/**
 * Resultado de la validación estructural de un archivo o texto JSON de copia de seguridad.
 */
sealed interface BackupValidationResult {
    data class Valid(
        val payload: BackupPayload,
        val summaryMessage: String
    ) : BackupValidationResult

    data class Invalid(
        val errorMessage: String
    ) : BackupValidationResult
}

/**
 * Motor de serialización, validación estricta y restauración de Copias de Seguridad (.json).
 */
object BackupJsonManager {

    const val BACKUP_FORMAT_SIGNATURE = "GYMTRACKER_LOCAL_BACKUP"
    const val CURRENT_BACKUP_VERSION = 1

    /**
     * Genera el nombre de archivo sugerido: `gymtracker_backup_YYYY-MM-DD.json`
     */
    fun buildDefaultBackupFileName(timestampMillis: Long = System.currentTimeMillis()): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(timestampMillis))
        return "gymtracker_backup_$dateStr.json"
    }

    /**
     * Calcula las estadísticas actuales de almacenamiento local.
     */
    fun calculateStorageStats(
        routines: List<RoutineWithOrderedExercises>,
        sessions: List<WorkoutSessionWithDetails>,
        exercises: List<ExerciseEntity>,
        weightLogs: List<WeightLogEntity>,
        dailyNutritionLogs: List<DailyNutritionLogEntity>,
        customMealBlocks: List<CustomMealBlockEntity>
    ): LocalStorageStats {
        val streakInfo = StreakCalculator.calculate(sessions.map { it.session })
        val xpProfile = RpgGamificationEngine.calculateAthleteXpProfile(
            sessions = sessions,
            weightLogsCount = weightLogs.size,
            hasLoggedNutritionToday = dailyNutritionLogs.isNotEmpty()
        )

        return LocalStorageStats(
            routinesCount = routines.size,
            savedSessionsCount = sessions.size,
            totalSetsCount = sessions.sumOf { it.sets.size },
            exercisesCount = exercises.size,
            customExercisesCount = exercises.count { it.isCustom },
            biometricWeightLogsCount = weightLogs.size,
            nutritionDaysCount = dailyNutritionLogs.size,
            customMealsCount = customMealBlocks.size,
            currentStreakDays = streakInfo.currentStreakDays,
            bestStreakDays = maxOf(streakInfo.currentStreakDays, streakInfo.activeDaysCount),
            totalPersonalRecords = xpProfile.totalPrsCount
        )
    }

    /**
     * Empaqueta absolutamente todo el estado de la aplicación en un JSON formateado.
     */
    fun exportFullBackupToJson(
        routines: List<RoutineWithOrderedExercises>,
        sessions: List<WorkoutSessionWithDetails>,
        exercises: List<ExerciseEntity>,
        weightLogs: List<WeightLogEntity>,
        dailyNutritionLogs: List<DailyNutritionLogEntity>,
        customMealBlocks: List<CustomMealBlockEntity>,
        exportTimestampMillis: Long = System.currentTimeMillis()
    ): String {
        val stats = calculateStorageStats(
            routines = routines,
            sessions = sessions,
            exercises = exercises,
            weightLogs = weightLogs,
            dailyNutritionLogs = dailyNutritionLogs,
            customMealBlocks = customMealBlocks
        )

        val root = JSONObject()
        root.put("signature", BACKUP_FORMAT_SIGNATURE)
        root.put("version", CURRENT_BACKUP_VERSION)
        root.put("exportedAtMillis", exportTimestampMillis)
        root.put(
            "exportedDateFormatted",
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(exportTimestampMillis))
        )

        // Resumen de metadatos, récords y racha
        val metaObj = JSONObject().apply {
            put("routinesCount", stats.routinesCount)
            put("savedSessionsCount", stats.savedSessionsCount)
            put("totalSetsCount", stats.totalSetsCount)
            put("exercisesCount", stats.exercisesCount)
            put("customExercisesCount", stats.customExercisesCount)
            put("biometricWeightLogsCount", stats.biometricWeightLogsCount)
            put("nutritionDaysCount", stats.nutritionDaysCount)
            put("customMealsCount", stats.customMealsCount)
            put("currentStreakDays", stats.currentStreakDays)
            put("bestStreakDays", stats.bestStreakDays)
            put("totalPersonalRecords", stats.totalPersonalRecords)
        }
        root.put("metadata", metaObj)

        // 1. Ejercicios (incluyendo personalizados y notas fijas técnicas)
        val exercisesArr = JSONArray()
        exercises.forEach { ex ->
            val obj = JSONObject().apply {
                put("id", ex.id)
                put("name", ex.name)
                put("muscleGroup", ex.muscleGroup)
                put("equipment", ex.equipment)
                put("isCustom", ex.isCustom)
                put("defaultRestSeconds", ex.defaultRestSeconds)
                put("thumbnailUrl", ex.thumbnailUrl)
                put("gifPathOrUrl", ex.gifPathOrUrl)
                put("primaryMuscle", ex.primaryMuscle)
                put("secondaryMuscles", ex.secondaryMuscles)
                put("instructions", ex.instructions)
                put("pinnedNote", ex.pinnedNote)
            }
            exercisesArr.put(obj)
        }
        root.put("exercises", exercisesArr)

        // 2. Rutinas y orden de ejercicios
        val routinesArr = JSONArray()
        routines.forEach { rWithEx ->
            val r = rWithEx.routine
            val exIdsArr = JSONArray()
            rWithEx.sortedExercises.forEach { exIdsArr.put(it.id) }

            val obj = JSONObject().apply {
                put("id", r.id)
                put("title", r.title)
                put("notes", r.notes)
                put("createdAt", r.createdAt)
                put("sortOrder", r.sortOrder)
                put("exerciseIds", exIdsArr)
            }
            routinesArr.put(obj)
        }
        root.put("routines", routinesArr)

        // 3. Historial de entrenamientos y series (incluye datos para PRs y racha)
        val sessionsArr = JSONArray()
        sessions.forEach { sWithDetails ->
            val s = sWithDetails.session
            val setsArr = JSONArray()
            sWithDetails.sets.forEach { setWithEx ->
                val set = setWithEx.set
                val setObj = JSONObject().apply {
                    put("id", set.id)
                    put("workoutSessionId", set.workoutSessionId)
                    put("exerciseId", set.exerciseId)
                    put("setNumber", set.setNumber)
                    put("weightKg", set.weightKg)
                    put("reps", set.reps)
                    put("setType", set.setType)
                    put("isCompleted", set.isCompleted)
                }
                setsArr.put(setObj)
            }

            val sObj = JSONObject().apply {
                put("id", s.id)
                if (s.routineId != null) put("routineId", s.routineId) else put("routineId", JSONObject.NULL)
                put("title", s.title)
                put("startTime", s.startTime)
                if (s.endTime != null) put("endTime", s.endTime) else put("endTime", JSONObject.NULL)
                put("durationSeconds", s.durationSeconds)
                put("totalVolumeKg", s.totalVolumeKg)
                put("sets", setsArr)
            }
            sessionsArr.put(sObj)
        }
        root.put("workoutSessions", sessionsArr)

        // 4. Historial de pesos corporales y medidas antropométricas (cinta métrica + comparador)
        val weightsArr = JSONArray()
        weightLogs.forEach { w ->
            val wObj = JSONObject().apply {
                put("id", w.id)
                put("weightKg", w.weightKg.toDouble())
                put("date", w.date)
                put("notes", w.notes ?: "")
                if (w.bicepsRelaxedCm != null) put("bicepsRelaxedCm", w.bicepsRelaxedCm.toDouble()) else put("bicepsRelaxedCm", JSONObject.NULL)
                val contracted = w.resolvedBicepsContractedCm
                if (contracted != null) put("bicepsContractedCm", contracted.toDouble()) else put("bicepsContractedCm", JSONObject.NULL)
                if (w.forearmCm != null) put("forearmCm", w.forearmCm.toDouble()) else put("forearmCm", JSONObject.NULL)
                if (w.armCm != null) put("armCm", w.armCm.toDouble()) else if (contracted != null) put("armCm", contracted.toDouble()) else put("armCm", JSONObject.NULL)
                if (w.chestCm != null) put("chestCm", w.chestCm.toDouble()) else put("chestCm", JSONObject.NULL)
                if (w.waistCm != null) put("waistCm", w.waistCm.toDouble()) else put("waistCm", JSONObject.NULL)
                if (w.glutesCm != null) put("glutesCm", w.glutesCm.toDouble()) else put("glutesCm", JSONObject.NULL)
                if (w.thighCm != null) put("thighCm", w.thighCm.toDouble()) else put("thighCm", JSONObject.NULL)
                if (w.calfCm != null) put("calfCm", w.calfCm.toDouble()) else put("calfCm", JSONObject.NULL)
                if (w.photoUri != null) put("photoUri", w.photoUri) else put("photoUri", JSONObject.NULL)
                if (w.poseType != null) put("poseType", w.poseType) else put("poseType", JSONObject.NULL)
            }
            weightsArr.put(wObj)
        }
        root.put("weightLogs", weightsArr)

        // 5. Nutrición diaria 1-Tap, creatina e hidratación
        val nutrArr = JSONArray()
        dailyNutritionLogs.forEach { n ->
            val nObj = JSONObject().apply {
                put("dateKey", n.dateKey)
                put("consumedCalories", n.consumedCalories)
                put("consumedProteinGrams", n.consumedProteinGrams.toDouble())
                put("consumedCarbsGrams", n.consumedCarbsGrams.toDouble())
                put("consumedFatsGrams", n.consumedFatsGrams.toDouble())
                put("creatineTaken", n.creatineTaken)
                put("waterMl", n.waterMl)
                put("tapHistoryJson", n.tapHistoryJson)
            }
            nutrArr.put(nObj)
        }
        root.put("dailyNutritionLogs", nutrArr)

        // 6. Bloques personalizados de comidas frecuentes
        val mealsArr = JSONArray()
        customMealBlocks.forEach { m ->
            val mObj = JSONObject().apply {
                put("id", m.id)
                put("name", m.name)
                put("proteinGrams", m.proteinGrams.toDouble())
                put("carbsGrams", m.carbsGrams.toDouble())
                put("fatsGrams", m.fatsGrams.toDouble())
                put("calories", m.calories)
            }
            mealsArr.put(mObj)
        }
        root.put("customMealBlocks", mealsArr)

        return root.toString(2)
    }

    /**
     * Valida rigurosamente la estructura del JSON antes de cargar los datos para evitar corromper la aplicación.
     */
    fun validateAndParseBackupJson(rawJson: String): BackupValidationResult {
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) {
            return BackupValidationResult.Invalid("El contenido JSON está vacío.")
        }

        return try {
            val root = JSONObject(trimmed)

            // Validar firma o presencia de las colecciones principales
            val signature = root.optString("signature", "")
            val hasKnownSections = root.has("routines") ||
                root.has("workoutSessions") ||
                root.has("weightLogs") ||
                root.has("exercises")

            if (signature != BACKUP_FORMAT_SIGNATURE && !hasKnownSections) {
                return BackupValidationResult.Invalid(
                    "Formato JSON no reconocido: falta la firma '$BACKUP_FORMAT_SIGNATURE' y las colecciones de datos."
                )
            }

            // 1. Validar y parsear Ejercicios
            val parsedExercises = mutableListOf<ExerciseEntity>()
            val exercisesArr = root.optJSONArray("exercises")
            if (exercisesArr != null) {
                for (i in 0 until exercisesArr.length()) {
                    val obj = exercisesArr.optJSONObject(i)
                        ?: return BackupValidationResult.Invalid("Ejercicio inválido en la posición $i.")
                    val name = obj.optString("name", "").trim()
                    if (name.isEmpty()) {
                        return BackupValidationResult.Invalid("El ejercicio en la posición $i no tiene nombre.")
                    }
                    parsedExercises.add(
                        ExerciseEntity(
                            id = obj.optLong("id", 0L),
                            name = name,
                            muscleGroup = obj.optString("muscleGroup", "General").ifBlank { "General" },
                            equipment = obj.optString("equipment", "Peso Libre").ifBlank { "Peso Libre" },
                            isCustom = obj.optBoolean("isCustom", false),
                            defaultRestSeconds = obj.optInt("defaultRestSeconds", 90).coerceAtLeast(10),
                            thumbnailUrl = obj.optString("thumbnailUrl", ""),
                            gifPathOrUrl = obj.optString("gifPathOrUrl", ""),
                            primaryMuscle = obj.optString("primaryMuscle", ""),
                            secondaryMuscles = obj.optString("secondaryMuscles", ""),
                            instructions = obj.optString("instructions", ""),
                            pinnedNote = obj.optString("pinnedNote", "")
                        )
                    )
                }
            }

            // 2. Validar y parsear Rutinas
            val parsedRoutines = mutableListOf<BackupRoutineRecord>()
            val routinesArr = root.optJSONArray("routines")
            if (routinesArr != null) {
                for (i in 0 until routinesArr.length()) {
                    val obj = routinesArr.optJSONObject(i)
                        ?: return BackupValidationResult.Invalid("Rutina inválida en la posición $i.")
                    val title = obj.optString("title", "").trim()
                    if (title.isEmpty()) {
                        return BackupValidationResult.Invalid("La rutina en la posición $i no tiene título.")
                    }
                    val exIds = mutableListOf<Long>()
                    val exIdsArr = obj.optJSONArray("exerciseIds")
                    if (exIdsArr != null) {
                        for (j in 0 until exIdsArr.length()) {
                            val exId = exIdsArr.optLong(j, -1L)
                            if (exId > 0L) exIds.add(exId)
                        }
                    }
                    parsedRoutines.add(
                        BackupRoutineRecord(
                            routine = RoutineEntity(
                                id = obj.optLong("id", 0L),
                                title = title,
                                notes = obj.optString("notes", ""),
                                createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                                sortOrder = obj.optInt("sortOrder", i + 1)
                            ),
                            exerciseIds = exIds
                        )
                    )
                }
            }

            // 3. Validar y parsear Sesiones de Entrenamiento + Series
            val parsedSessions = mutableListOf<BackupWorkoutSessionRecord>()
            val sessionsArr = root.optJSONArray("workoutSessions")
            if (sessionsArr != null) {
                for (i in 0 until sessionsArr.length()) {
                    val obj = sessionsArr.optJSONObject(i)
                        ?: return BackupValidationResult.Invalid("Sesión de entrenamiento inválida en la posición $i.")
                    val title = obj.optString("title", "Entrenamiento").ifBlank { "Entrenamiento" }
                    val startTime = obj.optLong("startTime", -1L)
                    if (startTime <= 0L) {
                        return BackupValidationResult.Invalid("La sesión '$title' tiene una fecha de inicio inválida.")
                    }

                    val routineId = if (obj.isNull("routineId")) null else obj.optLong("routineId", 0L).takeIf { it > 0L }
                    val endTime = if (obj.isNull("endTime")) null else obj.optLong("endTime", startTime)

                    val setsList = mutableListOf<WorkoutSetEntity>()
                    val setsArr = obj.optJSONArray("sets")
                    if (setsArr != null) {
                        for (j in 0 until setsArr.length()) {
                            val sObj = setsArr.optJSONObject(j)
                                ?: return BackupValidationResult.Invalid("Serie inválida en la sesión '$title'.")
                            val exId = sObj.optLong("exerciseId", -1L)
                            if (exId <= 0L) {
                                return BackupValidationResult.Invalid("Serie sin exerciseId válido en la sesión '$title'.")
                            }
                            setsList.add(
                                WorkoutSetEntity(
                                    id = sObj.optLong("id", 0L),
                                    workoutSessionId = obj.optLong("id", 0L),
                                    exerciseId = exId,
                                    setNumber = sObj.optInt("setNumber", j + 1).coerceAtLeast(1),
                                    weightKg = sObj.optDouble("weightKg", 0.0).coerceAtLeast(0.0),
                                    reps = sObj.optInt("reps", 0).coerceAtLeast(0),
                                    setType = sObj.optString("setType", "NORMAL").ifBlank { "NORMAL" },
                                    isCompleted = sObj.optBoolean("isCompleted", true)
                                )
                            )
                        }
                    }

                    parsedSessions.add(
                        BackupWorkoutSessionRecord(
                            session = WorkoutSessionEntity(
                                id = obj.optLong("id", 0L),
                                routineId = routineId,
                                title = title,
                                startTime = startTime,
                                endTime = endTime,
                                durationSeconds = obj.optLong("durationSeconds", 0L).coerceAtLeast(0L),
                                totalVolumeKg = obj.optDouble("totalVolumeKg", 0.0).coerceAtLeast(0.0)
                            ),
                            sets = setsList
                        )
                    )
                }
            }

            // 4. Validar y parsear Historial de Pesos y Medidas Antropométricas
            val parsedWeights = mutableListOf<WeightLogEntity>()
            val weightsArr = root.optJSONArray("weightLogs")
            if (weightsArr != null) {
                for (i in 0 until weightsArr.length()) {
                    val obj = weightsArr.optJSONObject(i)
                        ?: return BackupValidationResult.Invalid("Registro de peso inválido en la posición $i.")
                    val weightKg = obj.optDouble("weightKg", -1.0)
                    if (weightKg <= 0.0 || weightKg > 500.0) {
                        return BackupValidationResult.Invalid("Peso corporal fuera de rango válido en la posición $i.")
                    }
                    fun optNullableFloat(key: String): Float? {
                        if (!obj.has(key) || obj.isNull(key)) return null
                        val d = obj.optDouble(key, Double.NaN)
                        return if (!d.isNaN() && d > 0.0) d.toFloat() else null
                    }
                    fun optNullableString(key: String): String? {
                        if (!obj.has(key) || obj.isNull(key)) return null
                        return obj.optString(key, "").takeIf { it.isNotBlank() }
                    }

                    val parsedArm = optNullableFloat("armCm")
                    val parsedBicepsContracted = optNullableFloat("bicepsContractedCm") ?: parsedArm
                    parsedWeights.add(
                        WeightLogEntity(
                            id = obj.optLong("id", 0L),
                            weightKg = weightKg.toFloat(),
                            date = obj.optLong("date", System.currentTimeMillis()),
                            notes = obj.optString("notes", "").takeIf { it.isNotBlank() },
                            bicepsRelaxedCm = optNullableFloat("bicepsRelaxedCm"),
                            bicepsContractedCm = parsedBicepsContracted,
                            forearmCm = optNullableFloat("forearmCm"),
                            armCm = parsedArm ?: parsedBicepsContracted,
                            chestCm = optNullableFloat("chestCm"),
                            waistCm = optNullableFloat("waistCm"),
                            glutesCm = optNullableFloat("glutesCm"),
                            thighCm = optNullableFloat("thighCm"),
                            calfCm = optNullableFloat("calfCm"),
                            photoUri = optNullableString("photoUri"),
                            poseType = optNullableString("poseType")
                        )
                    )
                }
            }

            // 5. Validar y parsear Nutrición Diaria 1-Tap
            val parsedNutrition = mutableListOf<DailyNutritionLogEntity>()
            val nutrArr = root.optJSONArray("dailyNutritionLogs")
            if (nutrArr != null) {
                for (i in 0 until nutrArr.length()) {
                    val obj = nutrArr.optJSONObject(i) ?: continue
                    val dateKey = obj.optString("dateKey", "").trim()
                    if (dateKey.isNotEmpty()) {
                        parsedNutrition.add(
                            DailyNutritionLogEntity(
                                dateKey = dateKey,
                                consumedCalories = obj.optInt("consumedCalories", 0).coerceAtLeast(0),
                                consumedProteinGrams = obj.optDouble("consumedProteinGrams", 0.0).toFloat().coerceAtLeast(0f),
                                consumedCarbsGrams = obj.optDouble("consumedCarbsGrams", 0.0).toFloat().coerceAtLeast(0f),
                                consumedFatsGrams = obj.optDouble("consumedFatsGrams", 0.0).toFloat().coerceAtLeast(0f),
                                creatineTaken = obj.optBoolean("creatineTaken", false),
                                waterMl = obj.optInt("waterMl", 0).coerceAtLeast(0),
                                tapHistoryJson = obj.optString("tapHistoryJson", "[]")
                            )
                        )
                    }
                }
            }

            // 6. Validar y parsear Bloques Personalizados de Comidas
            val parsedCustomMeals = mutableListOf<CustomMealBlockEntity>()
            val mealsArr = root.optJSONArray("customMealBlocks")
            if (mealsArr != null) {
                for (i in 0 until mealsArr.length()) {
                    val obj = mealsArr.optJSONObject(i) ?: continue
                    val name = obj.optString("name", "").trim()
                    if (name.isNotEmpty()) {
                        parsedCustomMeals.add(
                            CustomMealBlockEntity(
                                id = obj.optLong("id", 0L),
                                name = name,
                                proteinGrams = obj.optDouble("proteinGrams", 0.0).toFloat().coerceAtLeast(0f),
                                carbsGrams = obj.optDouble("carbsGrams", 0.0).toFloat().coerceAtLeast(0f),
                                fatsGrams = obj.optDouble("fatsGrams", 0.0).toFloat().coerceAtLeast(0f),
                                calories = obj.optInt("calories", 0).coerceAtLeast(0)
                            )
                        )
                    }
                }
            }

            val metaObj = root.optJSONObject("metadata")
            val payload = BackupPayload(
                formatVersion = root.optInt("version", 1),
                exportedAtMillis = root.optLong("exportedAtMillis", System.currentTimeMillis()),
                exportedDateFormatted = root.optString("exportedDateFormatted", ""),
                currentStreakDays = metaObj?.optInt("currentStreakDays", 0) ?: 0,
                bestStreakDays = metaObj?.optInt("bestStreakDays", 0) ?: 0,
                totalPersonalRecords = metaObj?.optInt("totalPersonalRecords", 0) ?: 0,
                exercises = parsedExercises,
                routines = parsedRoutines,
                workoutSessions = parsedSessions,
                weightLogs = parsedWeights,
                dailyNutritionLogs = parsedNutrition,
                customMealBlocks = parsedCustomMeals
            )

            val summary = "Backup válido: ${parsedRoutines.size} rutinas, ${parsedSessions.size} entrenos, " +
                "${parsedExercises.size} ejercicios y ${parsedWeights.size} registros de peso."

            BackupValidationResult.Valid(payload = payload, summaryMessage = summary)
        } catch (e: Exception) {
            BackupValidationResult.Invalid(
                "Error de sintaxis JSON: ${e.localizedMessage ?: "archivo corrupto o mal formado"}"
            )
        }
    }
}
