package com.example.gymtracker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.domain.model.Workout
import com.example.gymtracker.domain.repository.GymRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeUiState(
    val activeWorkout: Workout? = null,
    val recentWorkouts: List<Workout> = emptyList(),
    val totalWorkoutsCompleted: Int = 0,
    val totalVolumeKg: Double = 0.0,
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val repository: GymRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getActiveWorkout(),
        repository.getAllWorkouts()
    ) { active, allWorkouts ->
        val completed = allWorkouts.filter { it.endTimeMillis != null }
        val totalVolume = completed.sumOf { it.totalVolumeKg }
        HomeUiState(
            activeWorkout = active,
            recentWorkouts = completed.take(5),
            totalWorkoutsCompleted = completed.size,
            totalVolumeKg = totalVolume,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun startNewWorkout(onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("EEEE, d MMM", Locale("es", "ES")).format(Date())
            val capitalized = dateStr.replaceFirstChar { it.uppercase() }
            val newWorkout = Workout(
                title = "Entrenamiento de $capitalized",
                startTimeMillis = System.currentTimeMillis()
            )
            val newId = repository.insertWorkout(newWorkout)
            onCreated(newId)
        }
    }

    class Factory(private val repository: GymRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
