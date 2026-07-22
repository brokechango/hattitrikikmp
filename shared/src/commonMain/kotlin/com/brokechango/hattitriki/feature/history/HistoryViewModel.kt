package com.brokechango.hattitriki.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brokechango.hattitriki.core.data.FootballSnapshotResult
import com.brokechango.hattitriki.core.data.FriendlyFootballRepository
import com.brokechango.hattitriki.core.model.FriendlyMatch
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
        loadHistory()
    }

    fun refresh() {
        if (!_uiState.value.isRefreshing) {
            loadHistory(isRefreshing = true)
        }
    }

    fun selectDateFilter(mode: HistoryDateFilterMode) {
        val latestDate = _uiState.value.matches
            .mapNotNull(FriendlyMatch::filterDate)
            .maxByOrNull(HistoryFilterDate::iso)

        _uiState.value = _uiState.value.copy(
            dateFilter = when (mode) {
                HistoryDateFilterMode.Month -> HistoryDateFilter(
                    mode = mode,
                    month = latestDate?.month,
                    year = latestDate?.year
                )
                HistoryDateFilterMode.Year -> HistoryDateFilter(
                    mode = mode,
                    year = latestDate?.year
                )
                HistoryDateFilterMode.Custom -> HistoryDateFilter(mode = mode)
            }
        )
    }

    fun clearDateFilter() {
        _uiState.value = _uiState.value.copy(dateFilter = HistoryDateFilter())
    }

    fun selectMonth(month: Int) {
        _uiState.value = _uiState.value.copy(
            dateFilter = _uiState.value.dateFilter.copy(month = month)
        )
    }

    fun selectYear(year: Int) {
        _uiState.value = _uiState.value.copy(
            dateFilter = _uiState.value.dateFilter.copy(year = year)
        )
    }

    fun selectCustomStartDate(date: String) {
        _uiState.value = _uiState.value.copy(
            dateFilter = _uiState.value.dateFilter.copy(startDate = date)
        )
    }

    fun selectCustomEndDate(date: String) {
        _uiState.value = _uiState.value.copy(
            dateFilter = _uiState.value.dateFilter.copy(endDate = date)
        )
    }

    private fun loadHistory(isRefreshing: Boolean = false) = viewModelScope.launch {
        if (isRefreshing) {
            _uiState.value = _uiState.value.copy(
                isRefreshing = true,
                errorMessage = null
            )
        }

        val leagueRepository = repository
        if (leagueRepository == null) {
            _uiState.value = HistoryUiState(
                isLoading = false,
                errorMessage = "Falta la configuración local de Supabase en este dispositivo."
            )
            return@launch
        }

        val dateFilter = _uiState.value.dateFilter
        _uiState.value = when (val result = leagueRepository.loadSnapshot()) {
            is FootballSnapshotResult.Success -> HistoryUiState(
                matches = result.snapshot.matches,
                dateFilter = dateFilter,
                isLoading = false
            )
            is FootballSnapshotResult.Failure -> _uiState.value.copy(
                isLoading = false,
                isRefreshing = false,
                errorMessage = result.message
            )
        }
    }
}
