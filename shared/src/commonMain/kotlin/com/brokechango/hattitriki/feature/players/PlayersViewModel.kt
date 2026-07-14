package com.brokechango.hattitriki.feature.players

import androidx.lifecycle.ViewModel
import com.brokechango.hattitriki.core.data.FriendlyFootballRepository
import com.brokechango.hattitriki.core.data.InMemoryFriendlyFootballRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayersViewModel(
    repository: FriendlyFootballRepository = InMemoryFriendlyFootballRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlayersUiState(players = repository.getStats()))
    val uiState: StateFlow<PlayersUiState> = _uiState.asStateFlow()
}
