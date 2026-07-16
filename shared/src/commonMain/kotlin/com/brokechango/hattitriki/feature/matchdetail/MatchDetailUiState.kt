package com.brokechango.hattitriki.feature.matchdetail

import com.brokechango.hattitriki.core.model.FriendlyMatch
import com.brokechango.hattitriki.core.model.Player

data class MatchDetailUiState(
    val match: FriendlyMatch? = null,
    val playersById: Map<String, Player> = emptyMap(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
