package com.brokechango.hattitriki.feature.players

import com.brokechango.hattitriki.core.model.PlayerStats
import com.brokechango.hattitriki.core.model.PlayerRankingCategory

data class PlayersUiState(
    val selectedCategory: PlayerRankingCategory = PlayerRankingCategory.TOP_SCORER,
    val rankings: List<PlayerRankingEntry> = emptyList(),
    val rankingView: RankingView = RankingView.COMPACT,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

data class PlayerRankingEntry(
    val stats: PlayerStats,
    val value: String,
    val recentForm: List<PlayerMatchResult>,
    val goalsAgainst: Int?
)

enum class RankingView(val label: String) {
    COMPACT("Compacto"),
    DETAILED("Detallado")
}

enum class PlayerMatchResult(val label: String) {
    WIN("V"),
    DRAW("E"),
    LOSS("D")
}
