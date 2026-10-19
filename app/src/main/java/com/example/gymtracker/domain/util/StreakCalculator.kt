package com.example.gymtracker.domain.util

import com.example.gymtracker.data.local.entity.WorkoutSessionEntity
import java.util.Calendar

data class StreakInfo(
    val activeDaysCount: Int,
    val consecutiveWeeksCompleted: Int,
    val currentStreakDays: Int,
    val workoutsThisMonth: Int,
    val isRestDayToday: Boolean,
    val recoveryStatusTitle: String,
    val motivationalMessage: String
)

/**
 * Calculador de racha inteligente para hipertrofia muscular.
 *
 * Principio:
 * El descanso es obligatorio para la reparación miofibrilar y la síntesis de proteína;
 * por tanto, los días de descanso planificados (hasta 2-3 días consecutivos) NO penalizan
 * ni rompen la racha.
 */
object StreakCalculator {

    private const val MAX_ALLOWED_REST_DAYS = 2 // Hasta 2 días de descanso seguidos entre entrenos (3 días de diferencia)
    private const val MIN_WORKOUTS_PER_WEEK = 2 // Mínimo de entrenamientos por semana para validar racha semanal

    fun calculate(
        sessions: List<WorkoutSessionEntity>,
        nowMillis: Long = System.currentTimeMillis()
    ): StreakInfo {
        val completedSessions = sessions.filter { it.endTime != null && it.startTime > 0 }

        if (completedSessions.isEmpty()) {
            return StreakInfo(
                activeDaysCount = 0,
                consecutiveWeeksCompleted = 0,
                currentStreakDays = 0,
                workoutsThisMonth = 0,
                isRestDayToday = true,
                recoveryStatusTitle = "Comienza tu Racha",
                motivationalMessage = "Inicia tu primer entrenamiento para activar tu racha de hipertrofia y constancia."
            )
        }

        val todayCal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val currentYear = todayCal.get(Calendar.YEAR)
        val currentMonth = todayCal.get(Calendar.MONTH)
        val todayEpochDay = todayCal.timeInMillis / (24L * 60 * 60 * 1000)

        // Mapear cada sesión completada a su epochDay único
        val sessionEpochDays = completedSessions.map { session ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = session.startTime
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis / (24L * 60 * 60 * 1000)
        }.distinct().sorted()

        val activeDaysCount = sessionEpochDays.size

        // Entrenamientos en el mes actual
        val workoutsThisMonth = completedSessions.count { session ->
            val cal = Calendar.getInstance().apply { timeInMillis = session.startTime }
            cal.get(Calendar.YEAR) == currentYear && cal.get(Calendar.MONTH) == currentMonth
        }

        // ¿Se entrenó hoy?
        val trainedToday = sessionEpochDays.contains(todayEpochDay)
        val isRestDayToday = !trainedToday

        // 1. Cálculo de Días de Racha Activa (con tolerancia a descansos de hipertrofia)
        val latestSessionDay = sessionEpochDays.last()
        val daysSinceLastWorkout = (todayEpochDay - latestSessionDay).toInt().coerceAtLeast(0)

        // Si han pasado más de 3 días desde el último entreno sin entrenar, la racha diaria expiró
        val isStreakAlive = daysSinceLastWorkout <= (MAX_ALLOWED_REST_DAYS + 1)

        val currentStreakDays: Int
        if (!isStreakAlive) {
            currentStreakDays = 0
        } else {
            // Recorrer hacia atrás los días de sesión
            var streakStartDay = sessionEpochDays.last()
            for (i in sessionEpochDays.size - 1 downTo 1) {
                val currentDay = sessionEpochDays[i]
                val prevDay = sessionEpochDays[i - 1]
                val gap = (currentDay - prevDay).toInt()
                if (gap <= (MAX_ALLOWED_REST_DAYS + 1)) {
                    streakStartDay = prevDay
                } else {
                    break
                }
            }
            // Días totales abarcados desde el inicio de la racha hasta hoy
            val rawStreak = (todayEpochDay - streakStartDay).toInt() + 1
            currentStreakDays = rawStreak.coerceAtLeast(1)
        }

        // 2. Cálculo de Semanas Consecutivas Cumplidas
        // Agrupar sesiones por semana del año
        val workoutsPerWeekKey = mutableMapOf<String, Int>()
        for (session in completedSessions) {
            val cal = Calendar.getInstance().apply {
                firstDayOfWeek = Calendar.MONDAY
                timeInMillis = session.startTime
            }
            val y = cal.get(Calendar.YEAR)
            val w = cal.get(Calendar.WEEK_OF_YEAR)
            val key = "$y-$w"
            workoutsPerWeekKey[key] = (workoutsPerWeekKey[key] ?: 0) + 1
        }

        // Evaluar semanas consecutivas hacia atrás
        var consecutiveWeeks = 0
        val checkCal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            timeInMillis = nowMillis
        }

        // Comprobar semana actual
        val currentWeekKey = "${checkCal.get(Calendar.YEAR)}-${checkCal.get(Calendar.WEEK_OF_YEAR)}"
        val workoutsCurrentWeek = workoutsPerWeekKey[currentWeekKey] ?: 0

        if (workoutsCurrentWeek >= MIN_WORKOUTS_PER_WEEK) {
            consecutiveWeeks++
        }

        // Comprobar semanas anteriores
        checkCal.add(Calendar.WEEK_OF_YEAR, -1)
        while (true) {
            val key = "${checkCal.get(Calendar.YEAR)}-${checkCal.get(Calendar.WEEK_OF_YEAR)}"
            val count = workoutsPerWeekKey[key] ?: 0
            if (count >= MIN_WORKOUTS_PER_WEEK) {
                consecutiveWeeks++
                checkCal.add(Calendar.WEEK_OF_YEAR, -1)
            } else {
                break
            }
        }

        // 3. Título y Mensaje Motivacional
        val statusTitle: String
        val motivationalMsg: String

        if (trainedToday) {
            statusTitle = "¡Entrenamiento Completado Hoy! 🔥"
            motivationalMsg = "Gran sesión realizada. Las fibras musculares han recibido el estímulo de hipertrofia. Prioriza proteína y descanso."
        } else if (isStreakAlive && isRestDayToday) {
            statusTitle = "Día de Recuperación Muscular 🛡️"
            motivationalMsg = "Racha activa: El descanso es parte obligatoria del plan. Tus músculos se reconstruyen y almacenan glucógeno hoy."
        } else if (currentStreakDays > 0) {
            statusTitle = "Racha Activa 🔥"
            motivationalMsg = "Excelente disciplina. Mantén el ritmo en tus días de entreno y respeta la recuperación."
        } else {
            statusTitle = "Listo para Reiniciar 🔥"
            motivationalMsg = "¡Hoy es un gran día para entrenar y comenzar una nueva racha de crecimiento muscular!"
        }

        return StreakInfo(
            activeDaysCount = activeDaysCount,
            consecutiveWeeksCompleted = consecutiveWeeks,
            currentStreakDays = currentStreakDays,
            workoutsThisMonth = workoutsThisMonth,
            isRestDayToday = isRestDayToday,
            recoveryStatusTitle = statusTitle,
            motivationalMessage = motivationalMsg
        )
    }
}
