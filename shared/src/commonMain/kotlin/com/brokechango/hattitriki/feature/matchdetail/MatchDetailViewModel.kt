package com.brokechango.hattitriki.feature.matchdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brokechango.hattitriki.core.data.FootballSnapshotResult
import com.brokechango.hattitriki.core.data.FriendlyFootballRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatchDetailViewModel(
    matchId: String,
    private val repository: FriendlyFootballRepository?
) : ViewModel() {
    private val _uiState = MutableStateFlow(MatchDetailUiState())
    val uiState: StateFlow<MatchDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val leagueRepository = repository
            if (leagueRepository == null) {
                _uiState.value = MatchDetailUiState(
                    isLoading = false,
                    errorMessage = "Falta la configuración local de Supabase en este dispositivo."
                )
                return@launch
            }

            _uiState.value = when (val result = leagueRepository.loadSnapshot()) {
                is FootballSnapshotResult.Success -> MatchDetailUiState(
                    match = result.snapshot.matches.firstOrNull { it.id == matchId },
                    playersById = result.snapshot.players.associateBy { it.id },
                    isLoading = false
                )
                is FootballSnapshotResult.Failure -> MatchDetailUiState(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }
}
