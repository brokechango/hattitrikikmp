package com.brokechango.hattitriki.feature.home

import androidx.lifecycle.ViewModel
import com.brokechango.hattitriki.core.data.FriendlyFootballRepository
import com.brokechango.hattitriki.core.data.InMemoryFriendlyFootballRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(
    private val repository: FriendlyFootballRepository = InMemoryFriendlyFootballRepository
) : ViewModel() {
    private val matches = repository.getMatches()
    private val stats = repository.getStats()

    private val _uiState = MutableStateFlow(
        HomeUiState(
            latestMatch = matches.firstOrNull(),
            topScorers = stats.take(3),
            totalMatches = matches.size,
            totalGoals = matches.sumOf { it.teamAScore + it.teamBScore }
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
