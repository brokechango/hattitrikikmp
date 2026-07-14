package com.brokechango.hattitriki.feature.matchdetail

import androidx.lifecycle.ViewModel
import com.brokechango.hattitriki.core.data.FriendlyFootballRepository
import com.brokechango.hattitriki.core.data.InMemoryFriendlyFootballRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MatchDetailViewModel(
    matchId: String,
    repository: FriendlyFootballRepository = InMemoryFriendlyFootballRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        MatchDetailUiState(
            match = repository.getMatch(matchId),
            playersById = repository.getPlayers().associateBy { it.id }
        )
    )
    val uiState: StateFlow<MatchDetailUiState> = _uiState.asStateFlow()
}
