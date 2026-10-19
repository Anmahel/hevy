package com.example.gymtracker.ui.screens.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.data.local.relation.CompletedMuscleSetRecord
import com.example.gymtracker.domain.model.DailyVolumeBar
import com.example.gymtracker.domain.model.HypertrophyMuscle
import com.example.gymtracker.domain.model.MuscleRecoveryItem
import com.example.gymtracker.domain.model.ProgressiveOverloadSummary
import com.example.gymtracker.domain.model.RecoveryChipState
import com.example.gymtracker.domain.model.StimulusClassification
import com.example.gymtracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class MuscleFilter(val title: String) {
    ALL("Todos"),
    OPTIMAL("En rango óptimo"),
    IN_RECOVERY("En recuperación"),
    READY("Listos para entrenar")
}

data class MuscleRecoveryUiState(
    val isLoading: Boolean = true,
    val muscleItems: List<MuscleRecoveryItem> = emptyList(),
    val filteredItems: List<MuscleRecoveryItem> = emptyList(),
    val overloadSummary: ProgressiveOverloadSummary = ProgressiveOverloadSummary(
        currentWeekVolumeKg = 0.0,
        previousWeekVolumeKg = 0.0,
        currentWeekSets = 0,
        previousWeekSets = 0,
        percentageDiff = 0.0,
        isOverloadAchieved = false,
        dailyBars = emptyList()
    ),
    val selectedFilter: MuscleFilter = MuscleFilter.ALL,
    val totalEffectiveSetsWeekly: Int = 0,
    val optimalMusclesCount: Int = 0,
    val inRecoveryMusclesCount: Int = 0
)

class MuscleProgressViewModel(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(MuscleFilter.ALL)

    val uiState: StateFlow<MuscleRecoveryUiState> = combine(
        workoutRepository.getAllCompletedMuscleSets(),
        _selectedFilter
    ) { sets, filter ->
        processMuscleData(sets, filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MuscleRecoveryUiState(isLoading = true)
    )

    fun setFilter(filter: MuscleFilter) {
        _selectedFilter.value = filter
    }

    private fun processMuscleData(
        sets: List<CompletedMuscleSetRecord>,
        filter: MuscleFilter
    ): MuscleRecoveryUiState {
        val now = System.currentTimeMillis()
        val sevenDaysMillis = 7L * 24 * 3600 * 1000
        val currentWeekStart = now - sevenDaysMillis
        val previousWeekStart = now - (14L * 24 * 3600 * 1000)

        val dateFormat = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault())

        data class WeightedMuscleSet(
            val muscle: HypertrophyMuscle,
            val record: CompletedMuscleSetRecord,
            val setWeightFactor: Double // 1.0 para músculo primario, 0.5 para secundario
        )

        // Mapear cada set completado a su músculo primario (1.0 serie) y secundarios (0.5 series), excluyendo calentamiento 'WARMUP'
        val mappedSets = sets.filter { !it.setType.equals("WARMUP", ignoreCase = true) }.flatMap { record ->
            val result = mutableListOf<WeightedMuscleSet>()
            val primary = HypertrophyMuscle.fromExerciseInfo(
                primaryMuscle = record.primaryMuscle,
                muscleGroup = record.muscleGroup,
                exerciseName = record.exerciseName
            )
            if (primary != null) {
                result.add(WeightedMuscleSet(primary, record, 1.0))
            }
            val secondaries = HypertrophyMuscle.fromSecondaryMuscles(
                secondaryMusclesText = record.secondaryMuscles,
                excludePrimary = primary
            )
            secondaries.forEach { secMuscle ->
                result.add(WeightedMuscleSet(secMuscle, record, 0.5))
            }
            result
        }

        val muscleItems = HypertrophyMuscle.ALL_ORDERED.map { targetMuscle ->
            val weightedSetsForMuscle = mappedSets.filter { it.muscle == targetMuscle }
            val setsForMuscle = weightedSetsForMuscle.map { it.record }

            // Sets en los últimos 7 días
            val currentWeekWeighted = weightedSetsForMuscle.filter {
                val time = it.record.sessionEndTime ?: it.record.sessionStartTime
                time in currentWeekStart..now
            }

            // Sets en la semana previa (días 8 a 14)
            val previousWeekWeighted = weightedSetsForMuscle.filter {
                val time = it.record.sessionEndTime ?: it.record.sessionStartTime
                time in previousWeekStart until currentWeekStart
            }

            val rawWeeklySets = currentWeekWeighted.sumOf { it.setWeightFactor }
            val rawPrevSets = previousWeekWeighted.sumOf { it.setWeightFactor }

            val weeklySetsCount = if (rawWeeklySets > 0.0) kotlin.math.ceil(rawWeeklySets - 0.001).toInt().coerceAtLeast(1) else 0
            val prevSetsCount = if (rawPrevSets > 0.0) kotlin.math.ceil(rawPrevSets - 0.001).toInt().coerceAtLeast(1) else 0

            val weeklyVolume = currentWeekWeighted.sumOf { (it.record.weightKg * it.record.reps) * it.setWeightFactor }
            val prevVolume = previousWeekWeighted.sumOf { (it.record.weightKg * it.record.reps) * it.setWeightFactor }

            val stimulus = StimulusClassification.fromWeeklySets(weeklySetsCount)

            // Buscar la última sesión en la que se entrenó este músculo
            val lastSet = setsForMuscle.maxByOrNull { it.sessionEndTime ?: it.sessionStartTime }
            val lastTrainedTime = lastSet?.let { it.sessionEndTime ?: it.sessionStartTime }

            val hoursSinceLastTrained: Long?
            val recoveryProgress: Float
            val recoveryMessage: String
            val chipState: RecoveryChipState
            val lastDateStr: String

            if (lastTrainedTime != null) {
                val elapsedMillis = (now - lastTrainedTime).coerceAtLeast(0L)
                val hours = elapsedMillis / (3600 * 1000L)
                hoursSinceLastTrained = hours
                lastDateStr = dateFormat.format(Date(lastTrainedTime))

                when {
                    hours <= 48 -> {
                        // 0 a 48 horas: Recuperando fibras (Barra de progreso llenándose)
                        recoveryProgress = (hours.toFloat() / 48f).coerceIn(0.08f, 0.98f)
                        recoveryMessage = "Recuperando fibras (${hours}h transcurridas)"
                        chipState = RecoveryChipState.IN_RECOVERY
                    }
                    hours in 49..72 -> {
                        // 48 a 72 horas: Recuperado y listo para volver a entrenar (100% óptimo)
                        recoveryProgress = 1.0f
                        recoveryMessage = "Recuperado y listo para volver a entrenar (${hours}h)"
                        chipState = RecoveryChipState.OPTIMAL
                    }
                    else -> {
                        // Más de 72 horas: Totalmente recuperado
                        recoveryProgress = 1.0f
                        val days = hours / 24
                        recoveryMessage = if (days >= 2) {
                            "Listo para entrenar (${days} días de descanso)"
                        } else {
                            "Listo para entrenar (${hours}h de descanso)"
                        }
                        chipState = RecoveryChipState.READY
                    }
                }
            } else {
                hoursSinceLastTrained = null
                recoveryProgress = 1.0f
                recoveryMessage = "Sin fatiga previa registrada. Fibras al 100%"
                chipState = RecoveryChipState.READY
                lastDateStr = "Sin registros previos"
            }

            MuscleRecoveryItem(
                muscle = targetMuscle,
                weeklySets = weeklySetsCount,
                previousWeekSets = prevSetsCount,
                weeklyVolumeKg = weeklyVolume,
                previousWeekVolumeKg = prevVolume,
                stimulus = stimulus,
                hoursSinceLastTrained = hoursSinceLastTrained,
                recoveryProgress = recoveryProgress,
                recoveryMessage = recoveryMessage,
                chipState = chipState,
                lastTrainedDateFormatted = lastDateStr
            )
        }

        // Resumen general de Sobrecarga Progresiva
        val currentWeekAllSets = sets.filter {
            val t = it.sessionEndTime ?: it.sessionStartTime
            t in currentWeekStart..now
        }
        val previousWeekAllSets = sets.filter {
            val t = it.sessionEndTime ?: it.sessionStartTime
            t in previousWeekStart until currentWeekStart
        }

        val totalCurrentVol = currentWeekAllSets.sumOf { it.weightKg * it.reps }
        val totalPrevVol = previousWeekAllSets.sumOf { it.weightKg * it.reps }

        val percentageDiff = if (totalPrevVol > 0) {
            ((totalCurrentVol - totalPrevVol) / totalPrevVol) * 100.0
        } else if (totalCurrentVol > 0) {
            100.0
        } else {
            0.0
        }

        val isOverload = totalCurrentVol > totalPrevVol && totalCurrentVol > 0

        // Barras diarias de los últimos 7 días
        val dailyBars = mutableListOf<DailyVolumeBar>()
        val dayLabelFormat = SimpleDateFormat("EEE", Locale("es", "ES"))
        val dateShortFormat = SimpleDateFormat("d MMM", Locale("es", "ES"))

        val calendar = Calendar.getInstance()
        val todayYear = calendar.get(Calendar.YEAR)
        val todayDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance()
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            dayCal.set(Calendar.HOUR_OF_DAY, 0)
            dayCal.set(Calendar.MINUTE, 0)
            dayCal.set(Calendar.SECOND, 0)
            dayCal.set(Calendar.MILLISECOND, 0)
            val dayStart = dayCal.timeInMillis

            dayCal.set(Calendar.HOUR_OF_DAY, 23)
            dayCal.set(Calendar.MINUTE, 59)
            dayCal.set(Calendar.SECOND, 59)
            dayCal.set(Calendar.MILLISECOND, 999)
            val dayEnd = dayCal.timeInMillis

            val setsInDay = sets.filter {
                val t = it.sessionEndTime ?: it.sessionStartTime
                t in dayStart..dayEnd
            }
            val dayVol = setsInDay.sumOf { it.weightKg * it.reps }

            val isToday = (dayCal.get(Calendar.YEAR) == todayYear && dayCal.get(Calendar.DAY_OF_YEAR) == todayDayOfYear)

            dailyBars.add(
                DailyVolumeBar(
                    dayLabel = dayLabelFormat.format(Date(dayStart)).replaceFirstChar { it.uppercase() },
                    dateFormatted = dateShortFormat.format(Date(dayStart)),
                    timestamp = dayStart,
                    volumeKg = dayVol,
                    setsCount = setsInDay.size,
                    isToday = isToday
                )
            )
        }

        val overloadSummary = ProgressiveOverloadSummary(
            currentWeekVolumeKg = totalCurrentVol,
            previousWeekVolumeKg = totalPrevVol,
            currentWeekSets = currentWeekAllSets.size,
            previousWeekSets = previousWeekAllSets.size,
            percentageDiff = percentageDiff,
            isOverloadAchieved = isOverload,
            dailyBars = dailyBars
        )

        // Filtrado
        val filteredList = when (filter) {
            MuscleFilter.ALL -> muscleItems
            MuscleFilter.OPTIMAL -> muscleItems.filter { it.chipState == RecoveryChipState.OPTIMAL || it.stimulus.isOptimal }
            MuscleFilter.IN_RECOVERY -> muscleItems.filter { it.chipState == RecoveryChipState.IN_RECOVERY }
            MuscleFilter.READY -> muscleItems.filter { it.chipState == RecoveryChipState.READY }
        }

        return MuscleRecoveryUiState(
            isLoading = false,
            muscleItems = muscleItems,
            filteredItems = filteredList,
            overloadSummary = overloadSummary,
            selectedFilter = filter,
            totalEffectiveSetsWeekly = muscleItems.sumOf { it.weeklySets },
            optimalMusclesCount = muscleItems.count { it.stimulus == StimulusClassification.OPTIMAL },
            inRecoveryMusclesCount = muscleItems.count { it.chipState == RecoveryChipState.IN_RECOVERY }
        )
    }

    class Factory(
        private val workoutRepository: WorkoutRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MuscleProgressViewModel(workoutRepository) as T
        }
    }
}
