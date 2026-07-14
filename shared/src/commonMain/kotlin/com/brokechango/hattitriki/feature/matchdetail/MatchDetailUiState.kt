package com.brokechango.hattitriki.feature.matchdetail

import com.brokechango.hattitriki.core.model.FriendlyMatch
import com.brokechango.hattitriki.core.model.Player

data class MatchDetailUiState(
    val match: FriendlyMatch?,
    val playersById: Map<String, Player>
)
