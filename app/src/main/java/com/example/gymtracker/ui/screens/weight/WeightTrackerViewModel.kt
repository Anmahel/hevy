package com.example.gymtracker.ui.screens.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.data.local.entity.CustomMealBlockEntity
import com.example.gymtracker.data.local.entity.DailyNutritionLogEntity
import com.example.gymtracker.data.local.entity.WeightLogEntity
import com.example.gymtracker.domain.repository.WeightRepository
import com.example.gymtracker.domain.util.MacroBreakdown
import com.example.gymtracker.domain.util.MacroCalculator
import com.example.gymtracker.domain.util.MealTapRecord
import com.example.gymtracker.domain.util.QuickMealBlock
import com.example.gymtracker.domain.util.QuickMealTrackerHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

enum class WeightSubTab(val title: String) {
    PESO("Peso & Macros"),
    HIDRATACION("Agua & Creatina"),
    MEDIDAS("Medidas"),
    COMPARADOR("Comparador")
}

data class NutritionCalendarDayUi(
    val dayOfMonth: Int,
    val dateKey: String,
    val displayDateFormatted: String,
    val waterMl: Int,
    val waterGoalMl: Int,
    val isHydrationGoalMet: Boolean,
    val hasAnyWater: Boolean,
    val creatineTaken: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val isFuture: Boolean
)

data class WeightUiItem(
    val entity: WeightLogEntity,
    val formattedDate: String,
    val shortDate: String,
    val formattedWeight: String,
    val percentageChange: Float?,
    val formattedPercentage: String?,
    val isPositive: Boolean?,
    // Diferencias de las 8 medidas antropométricas (+cm / -cm) respecto al registro anterior
    val bicepsRelaxedDiffCm: Float? = null,
    val bicepsContractedDiffCm: Float? = null,
    val forearmDiffCm: Float? = null,
    val armDiffCm: Float? = null,
    val chestDiffCm: Float? = null,
    val waistDiffCm: Float? = null,
    val glutesDiffCm: Float? = null,
    val thighDiffCm: Float? = null,
    val calfDiffCm: Float? = null,
    val isCleanMuscleGain: Boolean? = null
) {
    val hasMeasurements: Boolean
        get() = entity.bicepsRelaxedCm != null ||
            entity.resolvedBicepsContractedCm != null ||
            entity.forearmCm != null ||
            entity.chestCm != null ||
            entity.waistCm != null ||
            entity.glutesCm != null ||
            entity.thighCm != null ||
            entity.calfCm != null
}

data class WeightDialogState(
    val isOpen: Boolean = false,
    val editingId: Long? = null,
    val weightInput: String = "",
    val notesInput: String = "",
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val inputError: String? = null,
    // Cinta métrica (8 perímetros en cm)
    val bicepsRelaxedCmInput: String = "",
    val bicepsContractedCmInput: String = "",
    val forearmCmInput: String = "",
    val armCmInput: String = "",
    val chestCmInput: String = "",
    val waistCmInput: String = "",
    val glutesCmInput: String = "",
    val thighCmInput: String = "",
    val calfCmInput: String = "",
    // Comparador visual (Antes vs. Después)
    val photoUriInput: String = "",
    val poseTypeInput: String = "Doble Bíceps Frontal",
    val focusMeasurements: Boolean = false
)

data class CustomMealBlockDialogState(
    val isOpen: Boolean = false,
    val nameInput: String = "",
    val proteinInput: String = "",
    val carbsInput: String = "",
    val fatsInput: String = "",
    val caloriesInput: String = "",
    val inputError: String? = null
)

data class DailyNutritionUiState(
    val dateKey: String = "",
    val referenceWeightKg: Float = 75.0f,
    val isUsingFallbackWeight: Boolean = true,
    val targetMacros: MacroBreakdown = MacroCalculator.calculate(75.0f),
    val consumedCalories: Int = 0,
    val consumedProteinGrams: Float = 0f,
    val consumedCarbsGrams: Float = 0f,
    val consumedFatsGrams: Float = 0f,
    val creatineTaken: Boolean = false,
    val waterConsumedMl: Int = 0,
    val waterGoalMl: Int = QuickMealTrackerHelper.calculateDailyWaterGoalMl(75.0f),
    val tapHistory: List<MealTapRecord> = emptyList(),
    val mealBlocks: List<QuickMealBlock> = QuickMealTrackerHelper.defaultMealBlocks,
    // Unificación de Hidratación y Creatina con Calendario Mensual y Rachas
    val hydrationStreakDays: Int = 0,
    val creatineStreakDays: Int = 0,
    val totalHydrationDaysMet: Int = 0,
    val totalCreatineDaysMet: Int = 0,
    val calendarMonthTitle: String = "",
    val canGoNextMonth: Boolean = false,
    val calendarCells: List<NutritionCalendarDayUi?> = emptyList(),
    val selectedDayDetail: NutritionCalendarDayUi? = null
) {
    val canUndoLastTap: Boolean get() = tapHistory.isNotEmpty()
    val lastTapLabel: String? get() = tapHistory.lastOrNull()?.name
    val glassesTakenToday: Int get() = waterConsumedMl / 250
    val isWaterGoalMetToday: Boolean get() = waterGoalMl > 0 && waterConsumedMl >= waterGoalMl
}

private data class ComparatorSelection(
    val subTab: WeightSubTab = WeightSubTab.PESO,
    val beforeId: Long? = null,
    val afterId: Long? = null
)

private data class NutritionCalendarNavState(
    val monthOffset: Int = 0, // 0 = mes actual, -1 = mes anterior, etc.
    val selectedDateKey: String? = null,
    val todayDateKeyOverride: String? = null
)

data class WeightTrackerUiState(
    val items: List<WeightUiItem> = emptyList(),
    val latestWeightKg: Float? = null,
    val latestMacros: MacroBreakdown? = null,
    val totalWeightChangeKg: Float? = null,
    val selectedLogForMacros: WeightLogEntity? = null,
    val selectedMacroBreakdown: MacroBreakdown? = null,
    val dialogState: WeightDialogState = WeightDialogState(),
    val selectedSubTab: WeightSubTab = WeightSubTab.PESO,
    val compareBeforeItem: WeightUiItem? = null,
    val compareAfterItem: WeightUiItem? = null,
    // Módulo 3: Registro Rápido de Proteína 1-Tap y Suplementación Diaria Unificada
    val dailyNutrition: DailyNutritionUiState = DailyNutritionUiState(),
    val customMealDialogState: CustomMealBlockDialogState = CustomMealBlockDialogState()
) {
    val isAddEditDialogOpen: Boolean get() = dialogState.isOpen
    val editingLogId: Long? get() = dialogState.editingId
    val weightInput: String get() = dialogState.weightInput
    val notesInput: String get() = dialogState.notesInput
    val selectedDateMillis: Long get() = dialogState.selectedDateMillis
    val inputError: String get() = dialogState.inputError ?: ""
}

class WeightTrackerViewModel(
    private val weightRepository: WeightRepository
) : ViewModel() {

    private val _selectedLogForMacros = MutableStateFlow<WeightLogEntity?>(null)
    private val _dialogState = MutableStateFlow(WeightDialogState())
    private val _comparatorSelection = MutableStateFlow(ComparatorSelection())
    private val _customMealDialogState = MutableStateFlow(CustomMealBlockDialogState())
    private val _calendarNavState = MutableStateFlow(NutritionCalendarNavState())

    private val dateFormatter = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("es", "ES"))
    private val shortDateFormatter = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
    private val dayKeyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val monthTitleFormatter = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))

    private fun resolveTodayDateKey(): String {
        return _calendarNavState.value.todayDateKeyOverride
            ?: dayKeyFormatter.format(Date(System.currentTimeMillis()))
    }

    private data class NutritionBundle(
        val todayLog: DailyNutritionLogEntity?,
        val allLogs: List<DailyNutritionLogEntity>,
        val customBlocks: List<CustomMealBlockEntity>,
        val customDialog: CustomMealBlockDialogState,
        val calNav: NutritionCalendarNavState
    )

    private val nutritionFlows = combine(
        weightRepository.observeDailyNutritionLog(dayKeyFormatter.format(Date(System.currentTimeMillis()))),
        weightRepository.observeAllDailyNutritionLogs(),
        weightRepository.observeCustomMealBlocks(),
        _customMealDialogState,
        _calendarNavState
    ) { todayLog, allLogs, customBlocks, customDialog, calNav ->
        NutritionBundle(todayLog, allLogs, customBlocks, customDialog, calNav)
    }

    val uiState: StateFlow<WeightTrackerUiState> = combine(
        weightRepository.getAllWeightLogs(),
        _selectedLogForMacros,
        _dialogState,
        _comparatorSelection,
        nutritionFlows
    ) { rawLogs, selectedLog, dialog, compSel, nutrBundle ->
        val uiItems = rawLogs.mapIndexed { index, currentLog ->
            val prevLog = rawLogs.getOrNull(index + 1)
            val percentChange: Float?
            val formattedPercent: String?
            val isPos: Boolean?

            if (prevLog != null && prevLog.weightKg > 0f) {
                val diff = currentLog.weightKg - prevLog.weightKg
                val percent = (diff / prevLog.weightKg) * 100f
                percentChange = percent
                isPos = if (abs(diff) < 0.05f) null else diff > 0f
                formattedPercent = when {
                    abs(diff) < 0.05f -> "0.0%"
                    diff > 0f -> "+%.1f%%".format(Locale.US, percent)
                    else -> "%.1f%%".format(Locale.US, percent)
                }
            } else {
                percentChange = null
                formattedPercent = null
                isPos = null
            }

            // Buscar el registro anterior más cercano que tenga cada medida para calcular (+cm / -cm)
            val olderLogs = rawLogs.drop(index + 1)
            val prevBicepsRelaxed = olderLogs.firstOrNull { it.bicepsRelaxedCm != null }?.bicepsRelaxedCm
            val prevBicepsContracted = olderLogs.firstOrNull { it.resolvedBicepsContractedCm != null }?.resolvedBicepsContractedCm
            val prevForearm = olderLogs.firstOrNull { it.forearmCm != null }?.forearmCm
            val prevChest = olderLogs.firstOrNull { it.chestCm != null }?.chestCm
            val prevWaist = olderLogs.firstOrNull { it.waistCm != null }?.waistCm
            val prevGlutes = olderLogs.firstOrNull { it.glutesCm != null }?.glutesCm
            val prevThigh = olderLogs.firstOrNull { it.thighCm != null }?.thighCm
            val prevCalf = olderLogs.firstOrNull { it.calfCm != null }?.calfCm

            val currentContracted = currentLog.resolvedBicepsContractedCm

            val bicepsRelaxedDiff = if (currentLog.bicepsRelaxedCm != null && prevBicepsRelaxed != null) currentLog.bicepsRelaxedCm - prevBicepsRelaxed else null
            val bicepsContractedDiff = if (currentContracted != null && prevBicepsContracted != null) currentContracted - prevBicepsContracted else null
            val forearmDiff = if (currentLog.forearmCm != null && prevForearm != null) currentLog.forearmCm - prevForearm else null
            val chestDiff = if (currentLog.chestCm != null && prevChest != null) currentLog.chestCm - prevChest else null
            val waistDiff = if (currentLog.waistCm != null && prevWaist != null) currentLog.waistCm - prevWaist else null
            val glutesDiff = if (currentLog.glutesCm != null && prevGlutes != null) currentLog.glutesCm - prevGlutes else null
            val thighDiff = if (currentLog.thighCm != null && prevThigh != null) currentLog.thighCm - prevThigh else null
            val calfDiff = if (currentLog.calfCm != null && prevCalf != null) currentLog.calfCm - prevCalf else null

            val muscleGain = listOfNotNull(
                bicepsRelaxedDiff,
                bicepsContractedDiff,
                forearmDiff,
                chestDiff,
                glutesDiff,
                thighDiff,
                calfDiff
            ).any { it > 0.05f }

            val cleanGain: Boolean? = when {
                waistDiff == null && !muscleGain -> null
                muscleGain && (waistDiff == null || waistDiff <= 0.5f) -> true
                waistDiff != null && waistDiff > 1.0f && !muscleGain -> false
                waistDiff != null && waistDiff <= 0.2f -> true
                else -> null
            }

            val capDate = dateFormatter.format(Date(currentLog.date)).replaceFirstChar { it.uppercase() }
            val shortDateStr = shortDateFormatter.format(Date(currentLog.date))

            WeightUiItem(
                entity = currentLog,
                formattedDate = capDate,
                shortDate = shortDateStr,
                formattedWeight = "%.1f kg".format(Locale.US, currentLog.weightKg),
                percentageChange = percentChange,
                formattedPercentage = formattedPercent,
                isPositive = isPos,
                bicepsRelaxedDiffCm = bicepsRelaxedDiff,
                bicepsContractedDiffCm = bicepsContractedDiff,
                forearmDiffCm = forearmDiff,
                armDiffCm = bicepsContractedDiff,
                chestDiffCm = chestDiff,
                waistDiffCm = waistDiff,
                glutesDiffCm = glutesDiff,
                thighDiffCm = thighDiff,
                calfDiffCm = calfDiff,
                isCleanMuscleGain = cleanGain
            )
        }

        val latestLog = rawLogs.firstOrNull()
        val oldestLog = rawLogs.lastOrNull()
        val latestKg = latestLog?.weightKg
        val totalChange = if (latestLog != null && oldestLog != null && rawLogs.size > 1) {
            latestLog.weightKg - oldestLog.weightKg
        } else null

        val refWeightKg = latestKg ?: 75.0f
        val targetMacros = MacroCalculator.calculate(refWeightKg)
        val latestMacro = latestKg?.let { MacroCalculator.calculate(it) } ?: targetMacros
        val selectedMacro = selectedLog?.let { MacroCalculator.calculate(it.weightKg) }

        val resolvedBefore = uiItems.find { it.entity.id == compSel.beforeId }
            ?: uiItems.lastOrNull()
        val resolvedAfter = uiItems.find { it.entity.id == compSel.afterId }
            ?: uiItems.firstOrNull()

        val customMappedBlocks = nutrBundle.customBlocks.map { entity ->
            QuickMealBlock(
                id = "custom_${entity.id}",
                title = if (entity.name.startsWith("+")) entity.name else "+${entity.name}",
                proteinGrams = entity.proteinGrams,
                carbsGrams = entity.carbsGrams,
                fatsGrams = entity.fatsGrams,
                calories = entity.calories,
                isCustom = true,
                customEntityId = entity.id
            )
        }

        val allMealBlocks = QuickMealTrackerHelper.defaultMealBlocks + customMappedBlocks

        val activeTodayKey = nutrBundle.calNav.todayDateKeyOverride
            ?: dayKeyFormatter.format(Date(System.currentTimeMillis()))

        val logsByKey = buildMap {
            nutrBundle.allLogs.forEach { put(it.dateKey, it) }
            if (nutrBundle.todayLog != null) {
                put(nutrBundle.todayLog.dateKey, nutrBundle.todayLog)
            }
        }

        val activeTodayLog = logsByKey[activeTodayKey]
        val tapHistoryList = QuickMealTrackerHelper.parseTapHistory(activeTodayLog?.tapHistoryJson)
        val waterGoalMl = QuickMealTrackerHelper.calculateDailyWaterGoalMl(refWeightKg)

        // Calcular rachas y calendario mensual de Hidratación y Creatina
        val (hydrationStreak, creatineStreak) = calculateHydrationAndCreatineStreaks(
            logsByKey = logsByKey,
            todayKey = activeTodayKey,
            waterGoalMl = waterGoalMl
        )
        val totalHydrationMet = logsByKey.values.count { it.waterMl >= waterGoalMl && waterGoalMl > 0 }
        val totalCreatineMet = logsByKey.values.count { it.creatineTaken }

        val selectedDateKey = nutrBundle.calNav.selectedDateKey ?: activeTodayKey
        val (monthTitle, cells, selectedDayDetail) = buildNutritionCalendarMonth(
            monthOffset = nutrBundle.calNav.monthOffset,
            todayKey = activeTodayKey,
            selectedDateKey = selectedDateKey,
            logsByKey = logsByKey,
            waterGoalMl = waterGoalMl
        )

        val dailyNutritionUi = DailyNutritionUiState(
            dateKey = activeTodayKey,
            referenceWeightKg = refWeightKg,
            isUsingFallbackWeight = (latestKg == null),
            targetMacros = targetMacros,
            consumedCalories = activeTodayLog?.consumedCalories ?: 0,
            consumedProteinGrams = activeTodayLog?.consumedProteinGrams ?: 0f,
            consumedCarbsGrams = activeTodayLog?.consumedCarbsGrams ?: 0f,
            consumedFatsGrams = activeTodayLog?.consumedFatsGrams ?: 0f,
            creatineTaken = activeTodayLog?.creatineTaken ?: false,
            waterConsumedMl = activeTodayLog?.waterMl ?: 0,
            waterGoalMl = waterGoalMl,
            tapHistory = tapHistoryList,
            mealBlocks = allMealBlocks,
            hydrationStreakDays = hydrationStreak,
            creatineStreakDays = creatineStreak,
            totalHydrationDaysMet = totalHydrationMet,
            totalCreatineDaysMet = totalCreatineMet,
            calendarMonthTitle = monthTitle,
            canGoNextMonth = nutrBundle.calNav.monthOffset < 0,
            calendarCells = cells,
            selectedDayDetail = selectedDayDetail
        )

        WeightTrackerUiState(
            items = uiItems,
            latestWeightKg = latestKg,
            latestMacros = latestMacro,
            totalWeightChangeKg = totalChange,
            selectedLogForMacros = selectedLog,
            selectedMacroBreakdown = selectedMacro,
            dialogState = dialog,
            selectedSubTab = compSel.subTab,
            compareBeforeItem = resolvedBefore,
            compareAfterItem = resolvedAfter,
            dailyNutrition = dailyNutritionUi,
            customMealDialogState = nutrBundle.customDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WeightTrackerUiState()
    )

    private fun calculateHydrationAndCreatineStreaks(
        logsByKey: Map<String, DailyNutritionLogEntity>,
        todayKey: String,
        waterGoalMl: Int
    ): Pair<Int, Int> {
        val todayDate = runCatching { dayKeyFormatter.parse(todayKey) }.getOrNull() ?: Date()

        fun countStreak(predicate: (DailyNutritionLogEntity?) -> Boolean): Int {
            val cal = Calendar.getInstance().apply { time = todayDate }
            var streak = 0
            val todayLog = logsByKey[todayKey]
            if (predicate(todayLog)) {
                streak++
            } else {
                // Si hoy aún está en curso, comprobar desde ayer hacia atrás
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterdayKey = dayKeyFormatter.format(cal.time)
                if (!predicate(logsByKey[yesterdayKey])) {
                    return 0
                }
                streak++
            }

            while (true) {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val key = dayKeyFormatter.format(cal.time)
                if (predicate(logsByKey[key])) {
                    streak++
                } else {
                    break
                }
            }
            return streak
        }

        val hydrationStreak = countStreak { log -> log != null && waterGoalMl > 0 && log.waterMl >= waterGoalMl }
        val creatineStreak = countStreak { log -> log != null && log.creatineTaken }
        return Pair(hydrationStreak, creatineStreak)
    }

    private fun buildNutritionCalendarMonth(
        monthOffset: Int,
        todayKey: String,
        selectedDateKey: String,
        logsByKey: Map<String, DailyNutritionLogEntity>,
        waterGoalMl: Int
    ): Triple<String, List<NutritionCalendarDayUi?>, NutritionCalendarDayUi> {
        val todayDate = runCatching { dayKeyFormatter.parse(todayKey) }.getOrNull() ?: Date()
        val monthCal = Calendar.getInstance().apply {
            time = todayDate
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, monthOffset)
        }

        val monthTitle = monthTitleFormatter.format(monthCal.time).replaceFirstChar { it.uppercase() }
        val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Lunes = 0 .. Domingo = 6
        val firstDayOfWeek = monthCal.get(Calendar.DAY_OF_WEEK)
        val leadingEmptyCells = (firstDayOfWeek + 5) % 7

        val cells = mutableListOf<NutritionCalendarDayUi?>()
        repeat(leadingEmptyCells) { cells.add(null) }

        val year = monthCal.get(Calendar.YEAR)
        val month = monthCal.get(Calendar.MONTH)

        for (day in 1..daysInMonth) {
            val dayCal = Calendar.getInstance().apply {
                set(year, month, day, 12, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val key = dayKeyFormatter.format(dayCal.time)
            val log = logsByKey[key]
            val waterMl = log?.waterMl ?: 0
            val creatineTaken = log?.creatineTaken ?: false
            val displayFormatted = shortDateFormatter.format(dayCal.time)

            cells.add(
                NutritionCalendarDayUi(
                    dayOfMonth = day,
                    dateKey = key,
                    displayDateFormatted = if (key == todayKey) "Hoy ($displayFormatted)" else displayFormatted,
                    waterMl = waterMl,
                    waterGoalMl = waterGoalMl,
                    isHydrationGoalMet = waterGoalMl > 0 && waterMl >= waterGoalMl,
                    hasAnyWater = waterMl > 0,
                    creatineTaken = creatineTaken,
                    isToday = key == todayKey,
                    isSelected = key == selectedDateKey,
                    isFuture = key > todayKey
                )
            )
        }

        val selectedCell = cells.filterNotNull().find { it.dateKey == selectedDateKey }
            ?: run {
                val parsedSel = runCatching { dayKeyFormatter.parse(selectedDateKey) }.getOrNull() ?: todayDate
                val selCal = Calendar.getInstance().apply { time = parsedSel }
                val log = logsByKey[selectedDateKey]
                val waterMl = log?.waterMl ?: 0
                val displayFormatted = shortDateFormatter.format(selCal.time)
                NutritionCalendarDayUi(
                    dayOfMonth = selCal.get(Calendar.DAY_OF_MONTH),
                    dateKey = selectedDateKey,
                    displayDateFormatted = if (selectedDateKey == todayKey) "Hoy ($displayFormatted)" else displayFormatted,
                    waterMl = waterMl,
                    waterGoalMl = waterGoalMl,
                    isHydrationGoalMet = waterGoalMl > 0 && waterMl >= waterGoalMl,
                    hasAnyWater = waterMl > 0,
                    creatineTaken = log?.creatineTaken ?: false,
                    isToday = selectedDateKey == todayKey,
                    isSelected = true,
                    isFuture = selectedDateKey > todayKey
                )
            }

        return Triple(monthTitle, cells, selectedCell)
    }

    // --- Navegación del Calendario Mensual de Hidratación y Creatina ---

    fun onPreviousNutritionMonth() {
        _calendarNavState.update { it.copy(monthOffset = it.monthOffset - 1) }
    }

    fun onNextNutritionMonth() {
        _calendarNavState.update { state ->
            if (state.monthOffset < 0) state.copy(monthOffset = state.monthOffset + 1) else state
        }
    }

    fun onSelectNutritionCalendarDate(dateKey: String) {
        _calendarNavState.update { it.copy(selectedDateKey = dateKey) }
    }

    // --- Módulo 3: Acciones de Proteína y Comidas 1-Tap ---

    fun onQuickAddMealBlock(block: QuickMealBlock) {
        val activeTodayKey = resolveTodayDateKey()
        viewModelScope.launch {
            val current = weightRepository.getDailyNutritionLog(activeTodayKey)
                ?: DailyNutritionLogEntity(dateKey = activeTodayKey)
            val history = QuickMealTrackerHelper.parseTapHistory(current.tapHistoryJson).toMutableList()
            history.add(
                MealTapRecord(
                    name = block.title,
                    proteinGrams = block.proteinGrams,
                    carbsGrams = block.carbsGrams,
                    fatsGrams = block.fatsGrams,
                    calories = block.calories
                )
            )
            val totalProt = history.sumOf { it.proteinGrams.toDouble() }.toFloat()
            val totalCarbs = history.sumOf { it.carbsGrams.toDouble() }.toFloat()
            val totalFats = history.sumOf { it.fatsGrams.toDouble() }.toFloat()
            val totalKcal = history.sumOf { it.calories }

            weightRepository.upsertDailyNutritionLog(
                current.copy(
                    consumedCalories = totalKcal,
                    consumedProteinGrams = totalProt,
                    consumedCarbsGrams = totalCarbs,
                    consumedFatsGrams = totalFats,
                    tapHistoryJson = QuickMealTrackerHelper.encodeTapHistory(history)
                )
            )
        }
    }

    fun onUndoLastMealTap() {
        val activeTodayKey = resolveTodayDateKey()
        viewModelScope.launch {
            val current = weightRepository.getDailyNutritionLog(activeTodayKey) ?: return@launch
            val history = QuickMealTrackerHelper.parseTapHistory(current.tapHistoryJson).toMutableList()
            if (history.isEmpty()) return@launch
            history.removeAt(history.lastIndex)

            val totalProt = history.sumOf { it.proteinGrams.toDouble() }.toFloat()
            val totalCarbs = history.sumOf { it.carbsGrams.toDouble() }.toFloat()
            val totalFats = history.sumOf { it.fatsGrams.toDouble() }.toFloat()
            val totalKcal = history.sumOf { it.calories }

            weightRepository.upsertDailyNutritionLog(
                current.copy(
                    consumedCalories = totalKcal,
                    consumedProteinGrams = totalProt,
                    consumedCarbsGrams = totalCarbs,
                    consumedFatsGrams = totalFats,
                    tapHistoryJson = QuickMealTrackerHelper.encodeTapHistory(history)
                )
            )
        }
    }

    fun onResetDailyMacros() {
        val activeTodayKey = resolveTodayDateKey()
        viewModelScope.launch {
            val current = weightRepository.getDailyNutritionLog(activeTodayKey) ?: return@launch
            weightRepository.upsertDailyNutritionLog(
                current.copy(
                    consumedCalories = 0,
                    consumedProteinGrams = 0f,
                    consumedCarbsGrams = 0f,
                    consumedFatsGrams = 0f,
                    tapHistoryJson = "[]"
                )
            )
        }
    }

    // --- Módulo 3: Acciones de Creatina Diaria (5g con bloqueo por día) e Hidratación ---

    /**
     * Registra la toma de creatina del día de hoy (5g) y bloquea el estado por el resto del día.
     * Una vez marcada como tomada hoy, no se puede desmarcar hasta el día siguiente.
     */
    fun onTakeDailyCreatine() {
        val activeTodayKey = resolveTodayDateKey()
        viewModelScope.launch {
            val current = weightRepository.getDailyNutritionLog(activeTodayKey)
                ?: DailyNutritionLogEntity(dateKey = activeTodayKey)
            if (!current.creatineTaken) {
                weightRepository.upsertDailyNutritionLog(current.copy(creatineTaken = true))
            }
        }
    }

    fun onToggleCreatine(taken: Boolean) {
        val activeTodayKey = resolveTodayDateKey()
        viewModelScope.launch {
            val current = weightRepository.getDailyNutritionLog(activeTodayKey)
                ?: DailyNutritionLogEntity(dateKey = activeTodayKey)
            // Regla de bloqueo diario: si ya fue tomada hoy, permanece tomada (verde y bloqueada) por el resto del día
            if (current.creatineTaken) return@launch
            if (taken) {
                weightRepository.upsertDailyNutritionLog(current.copy(creatineTaken = true))
            }
        }
    }

    fun onAddWaterMl(deltaMl: Int) {
        if (deltaMl == 0) return
        val activeTodayKey = resolveTodayDateKey()
        viewModelScope.launch {
            val current = weightRepository.getDailyNutritionLog(activeTodayKey)
                ?: DailyNutritionLogEntity(dateKey = activeTodayKey)
            val newWater = (current.waterMl + deltaMl).coerceIn(0, 15000)
            weightRepository.upsertDailyNutritionLog(current.copy(waterMl = newWater))
        }
    }

    // --- Módulo 3: Creación y Eliminación de Bloques Personalizados ---

    fun onOpenCustomMealDialog() {
        _customMealDialogState.value = CustomMealBlockDialogState(isOpen = true)
    }

    fun onDismissCustomMealDialog() {
        _customMealDialogState.update { it.copy(isOpen = false, inputError = null) }
    }

    fun onCustomMealNameChanged(value: String) {
        _customMealDialogState.update { it.copy(nameInput = value, inputError = null) }
    }

    fun onCustomMealProteinChanged(value: String) {
        _customMealDialogState.update { state ->
            val updated = state.copy(proteinInput = value, inputError = null)
            updated.copy(caloriesInput = autoEstimateCalories(updated.proteinInput, updated.carbsInput, updated.fatsInput))
        }
    }

    fun onCustomMealCarbsChanged(value: String) {
        _customMealDialogState.update { state ->
            val updated = state.copy(carbsInput = value, inputError = null)
            updated.copy(caloriesInput = autoEstimateCalories(updated.proteinInput, updated.carbsInput, updated.fatsInput))
        }
    }

    fun onCustomMealFatsChanged(value: String) {
        _customMealDialogState.update { state ->
            val updated = state.copy(fatsInput = value, inputError = null)
            updated.copy(caloriesInput = autoEstimateCalories(updated.proteinInput, updated.carbsInput, updated.fatsInput))
        }
    }

    fun onCustomMealCaloriesChanged(value: String) {
        _customMealDialogState.update { it.copy(caloriesInput = value, inputError = null) }
    }

    private fun autoEstimateCalories(protStr: String, carbsStr: String, fatsStr: String): String {
        val p = protStr.trim().replace(',', '.').toFloatOrNull() ?: 0f
        val c = carbsStr.trim().replace(',', '.').toFloatOrNull() ?: 0f
        val f = fatsStr.trim().replace(',', '.').toFloatOrNull() ?: 0f
        val est = (p * 4f + c * 4f + f * 9f).roundToInt()
        return if (est > 0) est.toString() else ""
    }

    fun onSaveCustomMealBlock() {
        val dialog = _customMealDialogState.value
        val name = dialog.nameInput.trim()
        if (name.isEmpty()) {
            _customMealDialogState.update { it.copy(inputError = "Escribe el nombre de la comida (ej. Atún + Arroz)") }
            return
        }
        val prot = dialog.proteinInput.trim().replace(',', '.').toFloatOrNull() ?: 0f
        val carbs = dialog.carbsInput.trim().replace(',', '.').toFloatOrNull() ?: 0f
        val fats = dialog.fatsInput.trim().replace(',', '.').toFloatOrNull() ?: 0f
        val kcal = dialog.caloriesInput.trim().toIntOrNull()
            ?: (prot * 4f + carbs * 4f + fats * 9f).roundToInt()

        if (prot <= 0f && carbs <= 0f && fats <= 0f && kcal <= 0) {
            _customMealDialogState.update { it.copy(inputError = "Introduce al menos los gramos de proteína o calorías") }
            return
        }

        viewModelScope.launch {
            weightRepository.insertCustomMealBlock(
                CustomMealBlockEntity(
                    name = name,
                    proteinGrams = prot,
                    carbsGrams = carbs,
                    fatsGrams = fats,
                    calories = kcal.coerceAtLeast(1)
                )
            )
            _customMealDialogState.value = CustomMealBlockDialogState(isOpen = false)
        }
    }

    fun onDeleteCustomMealBlock(customId: Long) {
        viewModelScope.launch {
            weightRepository.deleteCustomMealBlockById(customId)
        }
    }

    // --- Acciones de Pestañas, Peso y Antropometría (8 Medidas Corporales) ---

    fun onSelectSubTab(tab: WeightSubTab) {
        _comparatorSelection.update { it.copy(subTab = tab) }
    }

    fun onSelectCompareBefore(logId: Long) {
        _comparatorSelection.update { it.copy(beforeId = logId) }
    }

    fun onSelectCompareAfter(logId: Long) {
        _comparatorSelection.update { it.copy(afterId = logId) }
    }

    fun onOpenAddDialog(focusMeasurements: Boolean = false) {
        val currentLatestItem = uiState.value.items.firstOrNull()?.entity
        val contractedStr = currentLatestItem?.resolvedBicepsContractedCm?.let { cm -> "%.1f".format(Locale.US, cm) } ?: ""
        _dialogState.update {
            it.copy(
                isOpen = true,
                editingId = null,
                weightInput = currentLatestItem?.weightKg?.let { kg -> "%.1f".format(Locale.US, kg) } ?: "",
                notesInput = "",
                selectedDateMillis = System.currentTimeMillis(),
                inputError = null,
                bicepsRelaxedCmInput = currentLatestItem?.bicepsRelaxedCm?.let { cm -> "%.1f".format(Locale.US, cm) } ?: "",
                bicepsContractedCmInput = contractedStr,
                forearmCmInput = currentLatestItem?.forearmCm?.let { cm -> "%.1f".format(Locale.US, cm) } ?: "",
                armCmInput = contractedStr,
                chestCmInput = currentLatestItem?.chestCm?.let { cm -> "%.1f".format(Locale.US, cm) } ?: "",
                waistCmInput = currentLatestItem?.waistCm?.let { cm -> "%.1f".format(Locale.US, cm) } ?: "",
                glutesCmInput = currentLatestItem?.glutesCm?.let { cm -> "%.1f".format(Locale.US, cm) } ?: "",
                thighCmInput = currentLatestItem?.thighCm?.let { cm -> "%.1f".format(Locale.US, cm) } ?: "",
                calfCmInput = currentLatestItem?.calfCm?.let { cm -> "%.1f".format(Locale.US, cm) } ?: "",
                photoUriInput = "",
                poseTypeInput = "Doble Bíceps Frontal",
                focusMeasurements = focusMeasurements
            )
        }
    }

    fun onOpenEditDialog(log: WeightLogEntity, focusMeasurements: Boolean = false) {
        val contractedStr = log.resolvedBicepsContractedCm?.let { cm -> "%.1f".format(Locale.US, cm) } ?: ""
        _dialogState.update {
            it.copy(
                isOpen = true,
                editingId = log.id,
                weightInput = "%.1f".format(Locale.US, log.weightKg),
                notesInput = log.notes ?: "",
                selectedDateMillis = log.date,
                inputError = null,
                bicepsRelaxedCmInput = log.bicepsRelaxedCm?.let { cm -> "%.1f".format(Locale.US, cm) } ?: "",
                bicepsContractedCmInput = contractedStr,
                forearmCmInput = log.forearmCm?.let { cm -> "%.1f".format(Locale.US, cm) } ?: "",
                armCmInput = contractedStr,
                chestCmInput = log.chestCm?.let { cm -> "%.1f".format(Locale.US, cm) } ?: "",
                waistCmInput = log.waistCm?.let { cm -> "%.1f".format(Locale.US, cm) } ?: "",
                glutesCmInput = log.glutesCm?.let { cm -> "%.1f".format(Locale.US, cm) } ?: "",
                thighCmInput = log.thighCm?.let { cm -> "%.1f".format(Locale.US, cm) } ?: "",
                calfCmInput = log.calfCm?.let { cm -> "%.1f".format(Locale.US, cm) } ?: "",
                photoUriInput = log.photoUri ?: "",
                poseTypeInput = log.poseType ?: "Doble Bíceps Frontal",
                focusMeasurements = focusMeasurements
            )
        }
    }

    fun onDismissDialog() {
        _dialogState.update { it.copy(isOpen = false, inputError = null) }
    }

    fun onWeightInputChanged(value: String) {
        _dialogState.update { it.copy(weightInput = value, inputError = null) }
    }

    fun onNotesInputChanged(value: String) {
        _dialogState.update { it.copy(notesInput = value) }
    }

    fun onDateChanged(dateMillis: Long) {
        _dialogState.update { it.copy(selectedDateMillis = dateMillis) }
    }

    fun onBicepsRelaxedCmChanged(value: String) {
        _dialogState.update { it.copy(bicepsRelaxedCmInput = value) }
    }

    fun onBicepsContractedCmChanged(value: String) {
        _dialogState.update { it.copy(bicepsContractedCmInput = value, armCmInput = value) }
    }

    fun onForearmCmChanged(value: String) {
        _dialogState.update { it.copy(forearmCmInput = value) }
    }

    fun onArmCmChanged(value: String) {
        _dialogState.update { it.copy(armCmInput = value, bicepsContractedCmInput = value) }
    }

    fun onChestCmChanged(value: String) {
        _dialogState.update { it.copy(chestCmInput = value) }
    }

    fun onWaistCmChanged(value: String) {
        _dialogState.update { it.copy(waistCmInput = value) }
    }

    fun onGlutesCmChanged(value: String) {
        _dialogState.update { it.copy(glutesCmInput = value) }
    }

    fun onThighCmChanged(value: String) {
        _dialogState.update { it.copy(thighCmInput = value) }
    }

    fun onCalfCmChanged(value: String) {
        _dialogState.update { it.copy(calfCmInput = value) }
    }

    fun onPhotoUriChanged(value: String) {
        _dialogState.update { it.copy(photoUriInput = value) }
    }

    fun onPoseTypeChanged(value: String) {
        _dialogState.update { it.copy(poseTypeInput = value) }
    }

    private fun parseOptionalCm(input: String): Float? {
        val clean = input.trim().replace(',', '.')
        if (clean.isEmpty()) return null
        val v = clean.toFloatOrNull() ?: return null
        return if (v in 10f..250f) v else null
    }

    fun onSaveWeightLog() {
        val currentDialog = _dialogState.value
        val sanitized = currentDialog.weightInput.trim().replace(',', '.')
        val parsedWeight = sanitized.toFloatOrNull()

        if (parsedWeight == null || parsedWeight <= 20f || parsedWeight >= 350f) {
            _dialogState.update { it.copy(inputError = "Introduce un peso válido entre 20 y 350 kg") }
            return
        }

        val parsedContracted = parseOptionalCm(currentDialog.bicepsContractedCmInput)
            ?: parseOptionalCm(currentDialog.armCmInput)

        viewModelScope.launch {
            val entity = WeightLogEntity(
                id = currentDialog.editingId ?: 0L,
                date = currentDialog.selectedDateMillis,
                weightKg = parsedWeight,
                notes = currentDialog.notesInput.trim().ifEmpty { null },
                bicepsRelaxedCm = parseOptionalCm(currentDialog.bicepsRelaxedCmInput),
                bicepsContractedCm = parsedContracted,
                forearmCm = parseOptionalCm(currentDialog.forearmCmInput),
                armCm = parsedContracted,
                chestCm = parseOptionalCm(currentDialog.chestCmInput),
                waistCm = parseOptionalCm(currentDialog.waistCmInput),
                glutesCm = parseOptionalCm(currentDialog.glutesCmInput),
                thighCm = parseOptionalCm(currentDialog.thighCmInput),
                calfCm = parseOptionalCm(currentDialog.calfCmInput),
                photoUri = currentDialog.photoUriInput.trim().ifEmpty { null },
                poseType = currentDialog.poseTypeInput.trim().ifEmpty { "Doble Bíceps Frontal" }
            )
            weightRepository.insertOrUpdateWeightLog(entity)
            _dialogState.update { it.copy(isOpen = false, inputError = null) }
        }
    }

    fun onSimulateBeforeAfterCards() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val sixWeeksAgo = now - (42L * 24L * 60L * 60L * 1000L)
            val baseWeight = uiState.value.latestWeightKg ?: 74.0f

            val beforeLog = WeightLogEntity(
                date = sixWeeksAgo,
                weightKg = baseWeight - 1.8f,
                notes = "Inicio bloque hipertrofia (Fase Base)",
                bicepsRelaxedCm = 34.5f,
                bicepsContractedCm = 37.5f,
                forearmCm = 29.5f,
                armCm = 37.5f,
                chestCm = 103.0f,
                waistCm = 80.5f,
                glutesCm = 96.0f,
                thighCm = 57.0f,
                calfCm = 38.2f,
                photoUri = "simulated://before_check",
                poseType = "Doble Bíceps Frontal"
            )
            val afterLog = WeightLogEntity(
                date = now,
                weightKg = baseWeight,
                notes = "Control semana 6 — Ganancia limpia sin subir cintura",
                bicepsRelaxedCm = 35.6f,
                bicepsContractedCm = 38.8f,
                forearmCm = 30.4f,
                armCm = 38.8f,
                chestCm = 105.5f,
                waistCm = 80.2f,
                glutesCm = 97.8f,
                thighCm = 58.8f,
                calfCm = 38.9f,
                photoUri = "simulated://after_check",
                poseType = "Doble Bíceps Frontal"
            )
            val beforeId = weightRepository.insertOrUpdateWeightLog(beforeLog)
            val afterId = weightRepository.insertOrUpdateWeightLog(afterLog)
            _comparatorSelection.update {
                it.copy(beforeId = beforeId, afterId = afterId)
            }
        }
    }

    fun onDeleteWeightLog(log: WeightLogEntity) {
        viewModelScope.launch {
            if (_selectedLogForMacros.value?.id == log.id) {
                _selectedLogForMacros.value = null
            }
            weightRepository.deleteWeightLog(log)
        }
    }

    fun onSelectLogForMacros(log: WeightLogEntity) {
        _selectedLogForMacros.value = log
    }

    fun onDismissMacroSheet() {
        _selectedLogForMacros.value = null
    }

    class Factory(
        private val weightRepository: WeightRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeightTrackerViewModel::class.java)) {
                return WeightTrackerViewModel(weightRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
