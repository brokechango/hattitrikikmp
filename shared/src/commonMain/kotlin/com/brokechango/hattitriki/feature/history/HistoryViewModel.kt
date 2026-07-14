package com.brokechango.hattitriki.feature.history

import androidx.lifecycle.ViewModel
import com.brokechango.hattitriki.core.data.FriendlyFootballRepository
import com.brokechango.hattitriki.core.data.InMemoryFriendlyFootballRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryViewModel(
    repository: FriendlyFootballRepository = InMemoryFriendlyFootballRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState(matches = repository.getMatches()))
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
}
