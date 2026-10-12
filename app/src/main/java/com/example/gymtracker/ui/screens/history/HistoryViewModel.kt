package com.example.gymtracker.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.data.local.relation.WorkoutSessionWithDetails
import com.example.gymtracker.domain.repository.GymRepository
import com.example.gymtracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HistoryUiState(
    val sessions: List<WorkoutSessionWithDetails> = emptyList(),
    val expandedSessionIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false
)

class HistoryViewModel(
    private val workoutRepository: WorkoutRepository,
    private val gymRepository: GymRepository? = null
) : ViewModel() {

    private val _expandedSessionIds = MutableStateFlow<Set<Long>>(emptySet())

    val uiState: StateFlow<HistoryUiState> = combine(
        workoutRepository.getWorkoutHistory(),
        _expandedSessionIds
    ) { historyList, expandedIds ->
        HistoryUiState(
            sessions = historyList,
            expandedSessionIds = expandedIds,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(isLoading = true)
    )

    fun toggleSessionExpanded(sessionId: Long) {
        val current = _expandedSessionIds.value
        _expandedSessionIds.value = if (current.contains(sessionId)) {
            current - sessionId
        } else {
            current + sessionId
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            workoutRepository.deleteWorkoutSession(sessionId)
            gymRepository?.deleteWorkout(sessionId)
        }
    }

    class Factory(
        private val workoutRepository: WorkoutRepository,
        private val gymRepository: GymRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(workoutRepository, gymRepository) as T
        }
    }
}
