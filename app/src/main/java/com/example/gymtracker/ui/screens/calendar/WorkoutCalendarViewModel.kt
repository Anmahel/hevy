package com.example.gymtracker.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.data.local.relation.WorkoutSessionWithDetails
import com.example.gymtracker.domain.repository.WorkoutRepository
import com.example.gymtracker.domain.util.StreakCalculator
import com.example.gymtracker.domain.util.StreakInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDayUi(
    val dayNumber: Int,
    val dateMillis: Long,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val hasWorkout: Boolean,
    val workoutsCount: Int,
    val isPlannedRestDay: Boolean
)

data class WorkoutCalendarUiState(
    val currentMonthTitle: String = "",
    val currentYear: Int = 2026,
    val currentMonth: Int = 9, // 0-based
    val days: List<CalendarDayUi> = emptyList(),
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val selectedDayFormatted: String = "",
    val selectedDayWorkouts: List<WorkoutSessionWithDetails> = emptyList(),
    val isSelectedDayRest: Boolean = false,
    val streakInfo: StreakInfo = StreakInfo(0, 0, 0, 0, true, "", ""),
    val isLoading: Boolean = false
)

class WorkoutCalendarViewModel(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val calendarInstance = Calendar.getInstance()
    private val _displayedYear = MutableStateFlow(calendarInstance.get(Calendar.YEAR))
    private val _displayedMonth = MutableStateFlow(calendarInstance.get(Calendar.MONTH)) // 0-based
    private val _selectedDateMillis = MutableStateFlow(calendarInstance.timeInMillis)

    private val monthTitleFormatter = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
    private val dayHeaderFormatter = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))

    val uiState: StateFlow<WorkoutCalendarUiState> = combine(
        workoutRepository.getWorkoutHistory(),
        _displayedYear,
        _displayedMonth,
        _selectedDateMillis
    ) { allWorkoutsWithDetails, year, month, selectedMillis ->
        val completedSessions = allWorkoutsWithDetails.filter { it.session.endTime != null }

        // Calcular Racha con StreakCalculator
        val streakInfo = StreakCalculator.calculate(
            sessions = completedSessions.map { it.session },
            nowMillis = System.currentTimeMillis()
        )

        // Agrupar entrenamientos por día calendario (clave: "YYYY-MM-DD")
        val workoutsByDayKey = mutableMapOf<String, MutableList<WorkoutSessionWithDetails>>()
        for (detail in completedSessions) {
            val cal = Calendar.getInstance().apply { timeInMillis = detail.session.startTime }
            val key = "%04d-%02d-%02d".format(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
            workoutsByDayKey.getOrOrPut(key) { mutableListOf() }.add(detail)
        }

        // Título del mes
        val monthCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val monthTitle = monthTitleFormatter.format(monthCal.time).replaceFirstChar { it.uppercase() }

        // Identificar 'hoy'
        val todayCal = Calendar.getInstance()
        val todayYear = todayCal.get(Calendar.YEAR)
        val todayMonth = todayCal.get(Calendar.MONTH)
        val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

        // Identificar día seleccionado
        val selectedCal = Calendar.getInstance().apply { timeInMillis = selectedMillis }
        val selYear = selectedCal.get(Calendar.YEAR)
        val selMonth = selectedCal.get(Calendar.MONTH)
        val selDay = selectedCal.get(Calendar.DAY_OF_MONTH)

        // Construir la cuadrícula mensual (Lunes a Domingo)
        val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = monthCal.get(Calendar.DAY_OF_WEEK)
        // Offset: Lunes=0, Martes=1, ..., Domingo=6
        val mondayBasedOffset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - Calendar.MONDAY

        // Días previos para rellenar la primera semana
        val prevMonthCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
        }
        val daysInPrevMonth = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val gridDays = mutableListOf<CalendarDayUi>()

        // 1. Días del mes anterior (padding)
        for (i in (daysInPrevMonth - mondayBasedOffset + 1)..daysInPrevMonth) {
            val dayCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, prevMonthCal.get(Calendar.YEAR))
                set(Calendar.MONTH, prevMonthCal.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            gridDays.add(
                CalendarDayUi(
                    dayNumber = i,
                    dateMillis = dayCal.timeInMillis,
                    isCurrentMonth = false,
                    isToday = false,
                    isSelected = false,
                    hasWorkout = false,
                    workoutsCount = 0,
                    isPlannedRestDay = false
                )
            )
        }

        // 2. Días del mes visible
        for (day in 1..daysInMonth) {
            val dayCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val dateKey = "%04d-%02d-%02d".format(year, month + 1, day)
            val dayWorkouts = workoutsByDayKey[dateKey] ?: emptyList()
            val hasWorkout = dayWorkouts.isNotEmpty()

            val isToday = (year == todayYear && month == todayMonth && day == todayDay)
            val isSelected = (year == selYear && month == selMonth && day == selDay)

            // Es día de descanso si no hubo entrenamiento y la fecha es hoy o pasada
            val isPastOrToday = dayCal.timeInMillis <= todayCal.timeInMillis
            val isRest = !hasWorkout && isPastOrToday

            gridDays.add(
                CalendarDayUi(
                    dayNumber = day,
                    dateMillis = dayCal.timeInMillis,
                    isCurrentMonth = true,
                    isToday = isToday,
                    isSelected = isSelected,
                    hasWorkout = hasWorkout,
                    workoutsCount = dayWorkouts.size,
                    isPlannedRestDay = isRest
                )
            )
        }

        // 3. Días del mes siguiente (para completar la última fila de 7)
        val remainingSlots = (7 - (gridDays.size % 7)) % 7
        for (nextDay in 1..remainingSlots) {
            val dayCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month + 1)
                set(Calendar.DAY_OF_MONTH, nextDay)
            }
            gridDays.add(
                CalendarDayUi(
                    dayNumber = nextDay,
                    dateMillis = dayCal.timeInMillis,
                    isCurrentMonth = false,
                    isToday = false,
                    isSelected = false,
                    hasWorkout = false,
                    workoutsCount = 0,
                    isPlannedRestDay = false
                )
            )
        }

        // Entrenamientos del día seleccionado
        val selectedKey = "%04d-%02d-%02d".format(selYear, selMonth + 1, selDay)
        val selectedDayWorkouts = workoutsByDayKey[selectedKey] ?: emptyList()
        val isSelectedRest = selectedDayWorkouts.isEmpty()

        val selectedFormatted = dayHeaderFormatter.format(Date(selectedMillis)).replaceFirstChar { it.uppercase() }

        WorkoutCalendarUiState(
            currentMonthTitle = monthTitle,
            currentYear = year,
            currentMonth = month,
            days = gridDays,
            selectedDateMillis = selectedMillis,
            selectedDayFormatted = selectedFormatted,
            selectedDayWorkouts = selectedDayWorkouts,
            isSelectedDayRest = isSelectedRest,
            streakInfo = streakInfo,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkoutCalendarUiState(isLoading = true)
    )

    fun onPreviousMonth() {
        if (_displayedMonth.value == 0) {
            _displayedMonth.value = 11
            _displayedYear.value -= 1
        } else {
            _displayedMonth.value -= 1
        }
    }

    fun onNextMonth() {
        if (_displayedMonth.value == 11) {
            _displayedMonth.value = 0
            _displayedYear.value += 1
        } else {
            _displayedMonth.value += 1
        }
    }

    fun onGoToToday() {
        val now = Calendar.getInstance()
        _displayedYear.value = now.get(Calendar.YEAR)
        _displayedMonth.value = now.get(Calendar.MONTH)
        _selectedDateMillis.value = now.timeInMillis
    }

    fun onSelectDay(day: CalendarDayUi) {
        _selectedDateMillis.value = day.dateMillis
        if (!day.isCurrentMonth) {
            val cal = Calendar.getInstance().apply { timeInMillis = day.dateMillis }
            _displayedYear.value = cal.get(Calendar.YEAR)
            _displayedMonth.value = cal.get(Calendar.MONTH)
        }
    }

    private fun <K, V> MutableMap<K, V>.getOrOrPut(key: K, defaultValue: () -> V): V {
        return this[key] ?: defaultValue().also { this[key] = it }
    }

    class Factory(
        private val workoutRepository: WorkoutRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WorkoutCalendarViewModel::class.java)) {
                return WorkoutCalendarViewModel(workoutRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
