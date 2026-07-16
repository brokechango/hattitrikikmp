package com.brokechango.hattitriki.feature.history

import com.brokechango.hattitriki.core.model.FriendlyMatch

data class HistoryUiState(
    val matches: List<FriendlyMatch> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
