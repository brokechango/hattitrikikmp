package com.brokechango.hattitriki.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brokechango.hattitriki.core.data.FootballSnapshotResult
import com.brokechango.hattitriki.core.data.FriendlyFootballRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: FriendlyFootballRepository?
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val leagueRepository = repository
            if (leagueRepository == null) {
                _uiState.value = HistoryUiState(
                    isLoading = false,
                    errorMessage = "Falta la configuración local de Supabase en este dispositivo."
                )
                return@launch
            }

            _uiState.value = when (val result = leagueRepository.loadSnapshot()) {
                is FootballSnapshotResult.Success -> HistoryUiState(
                    matches = result.snapshot.matches,
                    isLoading = false
                )
                is FootballSnapshotResult.Failure -> HistoryUiState(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }
}
