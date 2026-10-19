package com.example.gymtracker.ui.screens.stats

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.data.local.entity.ExerciseEntity
import com.example.gymtracker.data.local.relation.WorkoutSessionWithDetails
import com.example.gymtracker.domain.repository.ExerciseRepository
import com.example.gymtracker.domain.repository.WeightRepository
import com.example.gymtracker.domain.repository.WorkoutRepository
import com.example.gymtracker.domain.util.RpgGamificationEngine
import com.example.gymtracker.domain.util.StrengthRankInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Tipos de visualización estilo Recharts disponibles en la pantalla de Estadísticas de Rendimiento.
 */
enum class RechartsChartStyle(val label: String) {
    MONOTONE_LINE("Líneas 1RM"),
    AREA_GRADIENT("Área 1RM"),
    GROUPED_BARS("Barras 1RM")
}

/**
 * Representa un mes dentro de la ventana de los últimos 6 meses (Eje X de Recharts).
 */
data class MonthBucket(
    val index: Int,
    val year: Int,
    val month: Int, // 0..11
    val shortLabel: String, // ej. "May", "Jun", "Jul", "Ago", "Sep", "Oct"
    val fullLabel: String,  // ej. "Octubre 2026"
    val startMillis: Long,
    val endMillis: Long
)

/**
 * Punto de datos mensual de 1RM para un ejercicio específico.
 */
data class Monthly1RmPoint(
    val monthIndex: Int,
    val monthShortLabel: String,
    val monthFullLabel: String,
    val oneRepMaxKg: Double,
    val bestWeightKg: Double,
    val bestReps: Int,
    val hasRecordedSessionInMonth: Boolean
)

/**
 * Serie completa de 6 meses para uno de los 5 ejercicios principales del usuario.
 */
data class TopExercise1RmSeries(
    val rankOrder: Int, // 1..5
    val exerciseId: Long,
    val exerciseName: String,
    val shortName: String,
    val muscleGroup: String,
    val equipment: String,
    val thumbnailUrl: String,
    val gifPathOrUrl: String,
    val lineColor: Color,
    val points: List<Monthly1RmPoint>,
    val start1RmKg: Double,
    val currentBest1RmKg: Double,
    val deltaKg: Double,
    val deltaPercentage: Double,
    val totalCompletedSets: Int,
    val strengthRankInfo: StrengthRankInfo?
)

/**
 * Estado de la pantalla 'Estadísticas de Rendimiento' (Evolución 1RM de los 5 ejercicios principales en 6 meses).
 */
data class PerformanceStatsUiState(
    val isLoading: Boolean = false,
    val months: List<MonthBucket> = emptyList(),
    val top5Series: List<TopExercise1RmSeries> = emptyList(),
    val hiddenExerciseIds: Set<Long> = emptySet(),
    val selectedMonthIndex: Int = 5, // Por defecto el mes actual (último de los 6)
    val chartStyle: RechartsChartStyle = RechartsChartStyle.MONOTONE_LINE,
    val hasRealHistoricalData: Boolean = false,
    val isShowingDemoProjection: Boolean = false,
    val totalSessionsLast6Months: Int = 0,
    val totalSetsLast6Months: Int = 0,
    val bestOverallExerciseName: String = "—",
    val bestOverall1RmKg: Double = 0.0,
    val biggestGainerExerciseName: String = "—",
    val biggestGainerDeltaKg: Double = 0.0,
    val currentBodyweightKg: Double = 75.0
)

class PerformanceStatsViewModel(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val weightRepository: WeightRepository? = null
) : ViewModel() {

    private val _hiddenExerciseIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _selectedMonthIndex = MutableStateFlow(5)
    private val _chartStyle = MutableStateFlow(RechartsChartStyle.MONOTONE_LINE)
    private val _forceDemoProjection = MutableStateFlow<Boolean?>(null)

    companion object {
        // Paleta de 5 colores de alto contraste inspirada en Recharts Dark Neon para las 5 series
        val SERIES_COLORS = listOf(
            Color(0xFF00E676), // 1. Esmeralda Neón
            Color(0xFF00E5FF), // 2. Cian Eléctrico
            Color(0xFFFFD700), // 3. Oro Competitivo
            Color(0xFFB388FF), // 4. Amatista Vibrante
            Color(0xFFFF5252)  // 5. Coral Intenso
        )

        // Los 5 ejercicios principales canónicos por defecto si el usuario aún no tiene 5 ejercicios con historial
        val DEFAULT_MAIN_5_EXERCISES = listOf(
            "Press de banca plano con barra",
            "Sentadilla en máquina Smith",
            "Peso muerto rumano",
            "Prensa inclinada a 45°",
            "Jalón al pecho con agarre prono"
        )

        // Curvas base realistas de 6 meses (en kg de 1RM) para vista previa cuando aún no hay historial suficiente
        private val DEMO_BASE_CURVES = listOf(
            listOf(82.5, 86.0, 90.0, 93.5, 97.5, 102.5),  // Press de banca
            listOf(105.0, 112.0, 118.5, 124.0, 130.0, 136.5), // Sentadilla Smith
            listOf(95.0, 100.0, 106.0, 110.5, 116.0, 122.0),  // Peso muerto rumano
            listOf(160.0, 172.0, 185.0, 195.0, 208.0, 220.0), // Prensa 45°
            listOf(68.0, 71.5, 75.0, 78.0, 82.0, 86.0)        // Jalón al pecho
        )
    }

    private data class UserInteractionState(
        val hiddenIds: Set<Long>,
        val selectedMonthIdx: Int,
        val chartStyle: RechartsChartStyle,
        val forceDemo: Boolean?
    )

    private val interactionFlow = combine(
        _hiddenExerciseIds,
        _selectedMonthIndex,
        _chartStyle,
        _forceDemoProjection
    ) { hidden, monthIdx, style, forceDemo ->
        UserInteractionState(hidden, monthIdx, style, forceDemo)
    }

    val uiState: StateFlow<PerformanceStatsUiState> = combine(
        workoutRepository.getWorkoutHistory(),
        exerciseRepository.getAllExercises(),
        weightRepository?.getAllWeightLogs() ?: kotlinx.coroutines.flow.flowOf(emptyList()),
        interactionFlow
    ) { sessions, allExercises, weightLogs, interaction ->
        val monthBuckets = buildLast6MonthsBuckets()
        val sixMonthsStartMillis = monthBuckets.firstOrNull()?.startMillis ?: 0L
        val currentBodyweight = weightLogs.firstOrNull()?.weightKg?.toDouble() ?: 75.0

        val completedSessionsLast6M = sessions.filter { sessionDetail ->
            sessionDetail.session.startTime >= sixMonthsStartMillis &&
                sessionDetail.sets.any { it.set.isCompleted }
        }

        val totalSets6M = completedSessionsLast6M.sumOf { s ->
            s.sets.count { it.set.isCompleted }
        }

        // Seleccionar los 5 ejercicios principales del usuario
        val top5Exercises = selectTop5UserExercises(
            allExercises = allExercises,
            sessions = sessions
        )

        val hasAnyRealCompletedSet = sessions.any { s ->
            s.sets.any { it.set.isCompleted && it.set.weightKg > 0.0 && it.set.reps > 0 }
        }

        // Si el usuario no tiene entrenamientos registrados aún, activamos la proyección demo por defecto
        // para que la gráfica Recharts de 6 meses luzca completa desde el primer uso, permitiendo alternar.
        val useDemoProjection = interaction.forceDemo ?: !hasAnyRealCompletedSet

        val seriesList = top5Exercises.mapIndexed { idx, exercise ->
            val color = SERIES_COLORS[idx % SERIES_COLORS.size]
            if (useDemoProjection) {
                buildDemoSeriesForExercise(
                    rankOrder = idx + 1,
                    exercise = exercise,
                    monthBuckets = monthBuckets,
                    lineColor = color,
                    bodyweightKg = currentBodyweight
                )
            } else {
                buildRealSeriesForExercise(
                    rankOrder = idx + 1,
                    exercise = exercise,
                    monthBuckets = monthBuckets,
                    sessions = sessions,
                    lineColor = color,
                    bodyweightKg = currentBodyweight
                )
            }
        }

        val bestSeries = seriesList.maxByOrNull { it.currentBest1RmKg }
        val biggestGainer = seriesList.maxByOrNull { it.deltaKg }

        PerformanceStatsUiState(
            isLoading = false,
            months = monthBuckets,
            top5Series = seriesList,
            hiddenExerciseIds = interaction.hiddenIds,
            selectedMonthIndex = interaction.selectedMonthIdx.coerceIn(0, max(0, monthBuckets.lastIndex)),
            chartStyle = interaction.chartStyle,
            hasRealHistoricalData = hasAnyRealCompletedSet,
            isShowingDemoProjection = useDemoProjection,
            totalSessionsLast6Months = completedSessionsLast6M.size,
            totalSetsLast6Months = totalSets6M,
            bestOverallExerciseName = bestSeries?.shortName ?: "—",
            bestOverall1RmKg = bestSeries?.currentBest1RmKg ?: 0.0,
            biggestGainerExerciseName = biggestGainer?.shortName ?: "—",
            biggestGainerDeltaKg = biggestGainer?.deltaKg ?: 0.0,
            currentBodyweightKg = currentBodyweight
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PerformanceStatsUiState(isLoading = true)
    )

    fun selectMonthIndex(index: Int) {
        _selectedMonthIndex.value = index.coerceIn(0, 5)
    }

    fun toggleExerciseSeriesVisibility(exerciseId: Long) {
        val current = _hiddenExerciseIds.value.toMutableSet()
        if (current.contains(exerciseId)) {
            current.remove(exerciseId)
        } else {
            // Evitar ocultar las 5 líneas al mismo tiempo para mantener la gráfica legible
            if (current.size < 4) {
                current.add(exerciseId)
            }
        }
        _hiddenExerciseIds.value = current
    }

    fun setChartStyle(style: RechartsChartStyle) {
        _chartStyle.value = style
    }

    fun toggleDemoProjection() {
        val currentEffective = uiState.value.isShowingDemoProjection
        _forceDemoProjection.value = !currentEffective
    }

    /**
     * Construye los 6 contenedores mensuales correspondientes a los últimos 6 meses (incluyendo el mes actual).
     */
    private fun buildLast6MonthsBuckets(): List<MonthBucket> {
        val nowCal = Calendar.getInstance()
        val shortMonthFormat = SimpleDateFormat("MMM", Locale("es", "ES"))
        val fullMonthFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))

        return (5 downTo 0).mapIndexed { bucketIdx, monthsAgo ->
            val startCal = Calendar.getInstance().apply {
                timeInMillis = nowCal.timeInMillis
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MONTH, -monthsAgo)
            }

            val endCal = Calendar.getInstance().apply {
                timeInMillis = startCal.timeInMillis
                add(Calendar.MONTH, 1)
                add(Calendar.MILLISECOND, -1)
            }

            val rawShort = shortMonthFormat.format(startCal.time)
                .replace(".", "")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }

            val rawFull = fullMonthFormat.format(startCal.time)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }

            MonthBucket(
                index = bucketIdx,
                year = startCal.get(Calendar.YEAR),
                month = startCal.get(Calendar.MONTH),
                shortLabel = rawShort.take(3),
                fullLabel = rawFull,
                startMillis = startCal.timeInMillis,
                endMillis = endCal.timeInMillis
            )
        }
    }

    /**
     * Selecciona los 5 ejercicios principales del usuario:
     * 1. Prioriza ejercicios con mayor cantidad de series completadas y mayor 1RM histórico registrado.
     * 2. Si el usuario tiene menos de 5 ejercicios entrenados, completa con los básicos principales del catálogo.
     */
    private fun selectTop5UserExercises(
        allExercises: List<ExerciseEntity>,
        sessions: List<WorkoutSessionWithDetails>
    ): List<ExerciseEntity> {
        if (allExercises.isEmpty()) return emptyList()

        data class ExerciseUsageStats(
            val exerciseId: Long,
            val completedSetsCount: Int,
            val best1RmKg: Double
        )

        val statsMap = mutableMapOf<Long, ExerciseUsageStats>()
        sessions.forEach { sessionDetail ->
            sessionDetail.sets
                .filter { it.set.isCompleted && it.set.weightKg > 0.0 && it.set.reps > 0 }
                .forEach { setWithEx ->
                    val exId = setWithEx.set.exerciseId
                    val est1Rm = RpgGamificationEngine.calculateOneRepMax(
                        weightKg = setWithEx.set.weightKg,
                        reps = setWithEx.set.reps
                    )
                    val prev = statsMap[exId]
                    statsMap[exId] = ExerciseUsageStats(
                        exerciseId = exId,
                        completedSetsCount = (prev?.completedSetsCount ?: 0) + 1,
                        best1RmKg = max(prev?.best1RmKg ?: 0.0, est1Rm)
                    )
                }
        }

        val exerciseById = allExercises.associateBy { it.id }
        val rankedFromHistory = statsMap.values
            .sortedWith(
                compareByDescending<ExerciseUsageStats> { it.completedSetsCount }
                    .thenByDescending { it.best1RmKg }
            )
            .mapNotNull { exerciseById[it.exerciseId] }
            .toMutableList()

        if (rankedFromHistory.size >= 5) {
            return rankedFromHistory.take(5)
        }

        // Completar hasta 5 con los ejercicios compuestos principales del catálogo
        val selectedIds = rankedFromHistory.map { it.id }.toMutableSet()
        for (canonicalName in DEFAULT_MAIN_5_EXERCISES) {
            if (rankedFromHistory.size >= 5) break
            val match = allExercises.firstOrNull { ex ->
                ex.id !in selectedIds && ex.name.equals(canonicalName, ignoreCase = true)
            } ?: allExercises.firstOrNull { ex ->
                ex.id !in selectedIds && ex.name.contains(canonicalName.take(12), ignoreCase = true)
            }
            if (match != null) {
                rankedFromHistory.add(match)
                selectedIds.add(match.id)
            }
        }

        // Si aún faltan para llegar a 5, agregar del catálogo general
        for (ex in allExercises) {
            if (rankedFromHistory.size >= 5) break
            if (ex.id !in selectedIds) {
                rankedFromHistory.add(ex)
                selectedIds.add(ex.id)
            }
        }

        return rankedFromHistory.take(5)
    }

    /**
     * Construye la evolución real de 1RM mes a mes para un ejercicio a lo largo de los últimos 6 meses.
     */
    private fun buildRealSeriesForExercise(
        rankOrder: Int,
        exercise: ExerciseEntity,
        monthBuckets: List<MonthBucket>,
        sessions: List<WorkoutSessionWithDetails>,
        lineColor: Color,
        bodyweightKg: Double
    ): TopExercise1RmSeries {
        var totalSetsForExercise = 0
        var allTimeBest1Rm = 0.0

        // Calcular el mejor 1RM previo al inicio de la ventana de 6 meses para tener línea base continua
        val firstMonthStart = monthBuckets.firstOrNull()?.startMillis ?: 0L
        var runningBaseline1Rm = 0.0

        sessions.forEach { sessionDetail ->
            sessionDetail.sets
                .filter { it.set.isCompleted && it.set.exerciseId == exercise.id && it.set.weightKg > 0.0 && it.set.reps > 0 }
                .forEach { setWithEx ->
                    totalSetsForExercise++
                    val est1Rm = RpgGamificationEngine.calculateOneRepMax(setWithEx.set.weightKg, setWithEx.set.reps)
                    if (est1Rm > allTimeBest1Rm) {
                        allTimeBest1Rm = est1Rm
                    }
                    if (sessionDetail.session.startTime < firstMonthStart && est1Rm > runningBaseline1Rm) {
                        runningBaseline1Rm = est1Rm
                    }
                }
        }

        var carryForward1Rm = runningBaseline1Rm
        var carryWeight = 0.0
        var carryReps = 0

        val monthlyPoints = monthBuckets.map { bucket ->
            var bestMonth1Rm = 0.0
            var bestMonthWeight = 0.0
            var bestMonthReps = 0
            var hasSessionInMonth = false

            sessions
                .filter { it.session.startTime in bucket.startMillis..bucket.endMillis }
                .forEach { sessionDetail ->
                    sessionDetail.sets
                        .filter { it.set.isCompleted && it.set.exerciseId == exercise.id && it.set.weightKg > 0.0 && it.set.reps > 0 }
                        .forEach { setWithEx ->
                            hasSessionInMonth = true
                            val est1Rm = RpgGamificationEngine.calculateOneRepMax(
                                weightKg = setWithEx.set.weightKg,
                                reps = setWithEx.set.reps
                            )
                            if (est1Rm > bestMonth1Rm) {
                                bestMonth1Rm = est1Rm
                                bestMonthWeight = setWithEx.set.weightKg
                                bestMonthReps = setWithEx.set.reps
                            }
                        }
                }

            val effective1Rm = if (bestMonth1Rm > 0.0) {
                carryForward1Rm = bestMonth1Rm
                carryWeight = bestMonthWeight
                carryReps = bestMonthReps
                bestMonth1Rm
            } else {
                carryForward1Rm
            }

            Monthly1RmPoint(
                monthIndex = bucket.index,
                monthShortLabel = bucket.shortLabel,
                monthFullLabel = bucket.fullLabel,
                oneRepMaxKg = (effective1Rm * 10.0).roundToInt() / 10.0,
                bestWeightKg = if (hasSessionInMonth) bestMonthWeight else carryWeight,
                bestReps = if (hasSessionInMonth) bestMonthReps else carryReps,
                hasRecordedSessionInMonth = hasSessionInMonth
            )
        }

        val firstNonZero = monthlyPoints.firstOrNull { it.oneRepMaxKg > 0.0 }?.oneRepMaxKg ?: 0.0
        val currentBest = monthlyPoints.maxOfOrNull { it.oneRepMaxKg } ?: allTimeBest1Rm
        val deltaKg = if (firstNonZero > 0.0 && currentBest >= firstNonZero) {
            ((currentBest - firstNonZero) * 10.0).roundToInt() / 10.0
        } else {
            0.0
        }
        val deltaPct = if (firstNonZero > 0.0) {
            ((deltaKg / firstNonZero) * 1000.0).roundToInt() / 10.0
        } else {
            0.0
        }

        val rankInfo = RpgGamificationEngine.calculateStrengthRank(
            exerciseId = exercise.id,
            exerciseName = exercise.name,
            muscleGroup = exercise.primaryMuscle.ifEmpty { exercise.muscleGroup },
            bestOneRepMaxKg = currentBest,
            bodyweightKg = bodyweightKg
        )

        return TopExercise1RmSeries(
            rankOrder = rankOrder,
            exerciseId = exercise.id,
            exerciseName = exercise.name,
            shortName = shortenExerciseName(exercise.name),
            muscleGroup = exercise.primaryMuscle.ifEmpty { exercise.muscleGroup },
            equipment = exercise.equipment,
            thumbnailUrl = exercise.thumbnailUrl,
            gifPathOrUrl = exercise.gifPathOrUrl,
            lineColor = lineColor,
            points = monthlyPoints,
            start1RmKg = firstNonZero,
            currentBest1RmKg = currentBest,
            deltaKg = deltaKg,
            deltaPercentage = deltaPct,
            totalCompletedSets = totalSetsForExercise,
            strengthRankInfo = rankInfo
        )
    }

    /**
     * Construye una serie demostrativa de 6 meses basada en la progresión de hipertrofia de los 5 ejercicios principales.
     */
    private fun buildDemoSeriesForExercise(
        rankOrder: Int,
        exercise: ExerciseEntity,
        monthBuckets: List<MonthBucket>,
        lineColor: Color,
        bodyweightKg: Double
    ): TopExercise1RmSeries {
        val curve = DEMO_BASE_CURVES[(rankOrder - 1).coerceIn(0, DEMO_BASE_CURVES.lastIndex)]
        val points = monthBuckets.mapIndexed { idx, bucket ->
            val val1Rm = curve.getOrElse(idx) { curve.last() }
            val estWeight = ((val1Rm / (1.0 + 8.0 / 30.0)) * 2.0).roundToInt() / 2.0
            Monthly1RmPoint(
                monthIndex = bucket.index,
                monthShortLabel = bucket.shortLabel,
                monthFullLabel = bucket.fullLabel,
                oneRepMaxKg = val1Rm,
                bestWeightKg = estWeight,
                bestReps = 8,
                hasRecordedSessionInMonth = true
            )
        }

        val startVal = points.first().oneRepMaxKg
        val endVal = points.last().oneRepMaxKg
        val deltaKg = ((endVal - startVal) * 10.0).roundToInt() / 10.0
        val deltaPct = ((deltaKg / startVal) * 1000.0).roundToInt() / 10.0

        val rankInfo = RpgGamificationEngine.calculateStrengthRank(
            exerciseId = exercise.id,
            exerciseName = exercise.name,
            muscleGroup = exercise.primaryMuscle.ifEmpty { exercise.muscleGroup },
            bestOneRepMaxKg = endVal,
            bodyweightKg = bodyweightKg
        )

        return TopExercise1RmSeries(
            rankOrder = rankOrder,
            exerciseId = exercise.id,
            exerciseName = exercise.name,
            shortName = shortenExerciseName(exercise.name),
            muscleGroup = exercise.primaryMuscle.ifEmpty { exercise.muscleGroup },
            equipment = exercise.equipment,
            thumbnailUrl = exercise.thumbnailUrl,
            gifPathOrUrl = exercise.gifPathOrUrl,
            lineColor = lineColor,
            points = points,
            start1RmKg = startVal,
            currentBest1RmKg = endVal,
            deltaKg = deltaKg,
            deltaPercentage = deltaPct,
            totalCompletedSets = 24,
            strengthRankInfo = rankInfo
        )
    }

    private fun shortenExerciseName(fullName: String): String {
        return fullName
            .replace(" con barra", "", ignoreCase = true)
            .replace(" con mancuernas", "", ignoreCase = true)
            .replace(" en máquina Smith", " (Smith)", ignoreCase = true)
            .replace(" en máquina", "", ignoreCase = true)
            .replace(" con agarre prono", "", ignoreCase = true)
            .trim()
    }

    class Factory(
        private val workoutRepository: WorkoutRepository,
        private val exerciseRepository: ExerciseRepository,
        private val weightRepository: WeightRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PerformanceStatsViewModel::class.java)) {
                return PerformanceStatsViewModel(
                    workoutRepository = workoutRepository,
                    exerciseRepository = exerciseRepository,
                    weightRepository = weightRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
