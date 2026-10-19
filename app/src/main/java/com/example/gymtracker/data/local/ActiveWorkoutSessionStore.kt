package com.example.gymtracker.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.gymtracker.domain.model.SetType
import com.example.gymtracker.domain.model.WorkoutSet
import com.example.gymtracker.domain.model.WorkoutWithSets
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persistencia en tiempo real del entrenamiento en curso (Protección contra cierre en segundo plano).
 * Guarda de forma inmediata (JSON en SharedPreferences + sincronización con Room) el estado activo:
 * - workoutId y routineId activos
 * - startTimestamp real (para cálculo exacto con System.currentTimeMillis() - startTimestamp)
 * - Temporizador de descanso con restTimerStartTimestamp real
 * - Superseries (biseries) y ejercicio enfocado
 * - Snapshot completo en JSON de las series (pesos, repeticiones digitadas y checks marcados)
 */
data class PersistedActiveWorkoutState(
    val isActive: Boolean = false,
    val workoutId: Long = 0L,
    val routineId: Long = 0L,
    val title: String = "",
    val startTimestamp: Long = 0L,
    val restTimerActive: Boolean = false,
    val restTimerExpanded: Boolean = false,
    val restTimerStartTimestamp: Long = 0L,
    val restTimerTotalSeconds: Int = 90,
    val supersetPairs: Map<Long, Long> = emptyMap(),
    val focusedExerciseId: Long? = null,
    val setsSnapshot: List<WorkoutSet> = emptyList()
)

object ActiveWorkoutSessionStore {
    private const val PREFS_NAME = "active_workout_realtime_store"
    private const val KEY_STATE_JSON = "active_workout_state_json"

    @Volatile
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun prefs(context: Context? = appContext): SharedPreferences? {
        val ctx = context?.applicationContext ?: appContext ?: return null
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Guarda de forma síncrona/inmediata el estado completo del entrenamiento activo en JSON.
     */
    fun saveState(
        workoutId: Long,
        routineId: Long,
        title: String,
        startTimestamp: Long,
        restTimerActive: Boolean,
        restTimerExpanded: Boolean,
        restTimerStartTimestamp: Long,
        restTimerTotalSeconds: Int,
        supersetPairs: Map<Long, Long>,
        focusedExerciseId: Long?,
        workoutWithSets: WorkoutWithSets?,
        context: Context? = appContext
    ) {
        if (workoutId <= 0L) return
        val sp = prefs(context) ?: return
        try {
            val root = JSONObject().apply {
                put("isActive", true)
                put("workoutId", workoutId)
                put("routineId", routineId)
                put("title", workoutWithSets?.workout?.title?.ifBlank { title } ?: title)
                put("startTimestamp", workoutWithSets?.workout?.startTimeMillis ?: startTimestamp)
                put("restTimerActive", restTimerActive)
                put("restTimerExpanded", restTimerExpanded)
                put("restTimerStartTimestamp", restTimerStartTimestamp)
                put("restTimerTotalSeconds", restTimerTotalSeconds)
                put("focusedExerciseId", focusedExerciseId ?: -1L)

                val supersetObj = JSONObject()
                supersetPairs.forEach { (k, v) ->
                    supersetObj.put(k.toString(), v)
                }
                put("supersetPairs", supersetObj)

                val setsArray = JSONArray()
                val allSets = workoutWithSets?.exercises?.flatMap { it.sets }.orEmpty()
                allSets.forEach { s ->
                    val setJson = JSONObject().apply {
                        put("id", s.id)
                        put("workoutId", s.workoutId)
                        put("exerciseId", s.exerciseId)
                        put("exerciseName", s.exerciseName)
                        put("setNumber", s.setNumber)
                        put("weightKg", s.weightKg)
                        put("reps", s.reps)
                        put("isCompleted", s.isCompleted)
                        put("rpe", s.rpe ?: JSONObject.NULL)
                        put("previousRecord", s.previousRecord)
                        put("setType", s.setType.name)
                        put("exerciseOrder", s.exerciseOrder)
                        put("targetRepRange", s.targetRepRange)
                    }
                    setsArray.put(setJson)
                }
                put("setsSnapshot", setsArray)
                put("savedAtMillis", System.currentTimeMillis())
            }
            sp.edit().putString(KEY_STATE_JSON, root.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Recupera el estado persistido del entrenamiento en curso, o null si no hay sesión activa.
     */
    fun loadState(context: Context? = appContext): PersistedActiveWorkoutState? {
        val sp = prefs(context) ?: return null
        val raw = sp.getString(KEY_STATE_JSON, null) ?: return null
        return try {
            val root = JSONObject(raw)
            if (!root.optBoolean("isActive", false)) return null
            val workoutId = root.optLong("workoutId", 0L)
            if (workoutId <= 0L) return null

            val supersetPairs = mutableMapOf<Long, Long>()
            root.optJSONObject("supersetPairs")?.let { obj ->
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val kStr = keys.next()
                    val k = kStr.toLongOrNull()
                    val v = obj.optLong(kStr, -1L)
                    if (k != null && v > 0L) {
                        supersetPairs[k] = v
                    }
                }
            }

            val setsSnapshot = mutableListOf<WorkoutSet>()
            root.optJSONArray("setsSnapshot")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val item = arr.optJSONObject(i) ?: continue
                    val setTypeStr = item.optString("setType", SetType.NORMAL.name)
                    val setType = try {
                        SetType.valueOf(setTypeStr.uppercase())
                    } catch (_: Exception) {
                        SetType.NORMAL
                    }
                    setsSnapshot.add(
                        WorkoutSet(
                            id = item.optLong("id", 0L),
                            workoutId = item.optLong("workoutId", workoutId),
                            exerciseId = item.optLong("exerciseId", 0L),
                            exerciseName = item.optString("exerciseName", ""),
                            setNumber = item.optInt("setNumber", 1),
                            weightKg = item.optDouble("weightKg", 0.0),
                            reps = item.optInt("reps", 0),
                            isCompleted = item.optBoolean("isCompleted", false),
                            rpe = if (item.isNull("rpe")) null else item.optDouble("rpe"),
                            previousRecord = item.optString("previousRecord", "-"),
                            setType = setType,
                            exerciseOrder = item.optInt("exerciseOrder", 0),
                            targetRepRange = item.optString("targetRepRange", "")
                        )
                    )
                }
            }

            val focusedRaw = root.optLong("focusedExerciseId", -1L)

            PersistedActiveWorkoutState(
                isActive = true,
                workoutId = workoutId,
                routineId = root.optLong("routineId", 0L),
                title = root.optString("title", "Entrenamiento"),
                startTimestamp = root.optLong("startTimestamp", System.currentTimeMillis()),
                restTimerActive = root.optBoolean("restTimerActive", false),
                restTimerExpanded = root.optBoolean("restTimerExpanded", false),
                restTimerStartTimestamp = root.optLong("restTimerStartTimestamp", 0L),
                restTimerTotalSeconds = root.optInt("restTimerTotalSeconds", 90),
                supersetPairs = supersetPairs,
                focusedExerciseId = if (focusedRaw > 0L) focusedRaw else null,
                setsSnapshot = setsSnapshot
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Limpia el estado activo cuando el entrenamiento se finaliza o se descarta.
     */
    fun clearState(context: Context? = appContext) {
        val sp = prefs(context) ?: return
        sp.edit().remove(KEY_STATE_JSON).apply()
    }
}
