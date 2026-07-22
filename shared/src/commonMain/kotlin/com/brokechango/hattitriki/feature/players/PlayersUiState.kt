package com.brokechango.hattitriki.feature.players

import com.brokechango.hattitriki.core.model.PlayerStats
import com.brokechango.hattitriki.core.model.PlayerRankingCategory

data class PlayersUiState(
    val selectedCategory: PlayerRankingCategory = PlayerRankingCategory.TOP_SCORER,
    val rankings: List<PlayerRankingEntry> = emptyList(),
    val avatarUrlsByPlayerId: Map<String, String> = emptyMap(),
    val rankingView: RankingView = RankingView.COMPACT,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

data class PlayerRankingEntry(
    val stats: PlayerStats,
    val value: String,
    val recentForm: List<PlayerMatchResult>,
    val goalsAgainst: Double?
)

enum class RankingView(val label: String) {
    COMPACT("Compacto"),
    DETAILED("Detallado")
}

enum class PlayerMatchResult(val label: String) {
    WIN("V"),
    DRAW("E"),
    LOSS("D"),
    DID_NOT_PLAY("")
}
