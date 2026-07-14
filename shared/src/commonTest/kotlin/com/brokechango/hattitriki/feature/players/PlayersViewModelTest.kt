package com.brokechango.hattitriki.feature.players

import kotlin.test.Test
import kotlin.test.assertEquals

class PlayersViewModelTest {

    @Test
    fun detailedViewShowsEachPlayersFiveMostRecentResults() {
        val viewModel = PlayersViewModel()

        viewModel.selectRankingView(RankingView.DETAILED)

        val alex = viewModel.uiState.value.rankings.first { it.stats.player.id == "alex" }
        assertEquals(RankingView.DETAILED, viewModel.uiState.value.rankingView)
        assertEquals(
            listOf(
                PlayerMatchResult.WIN,
                PlayerMatchResult.LOSS,
                PlayerMatchResult.WIN,
                PlayerMatchResult.DRAW,
                PlayerMatchResult.LOSS
            ),
            alex.recentForm
        )
    }

    @Test
    fun changingCategoryKeepsTheChosenRankingView() {
        val viewModel = PlayersViewModel()

        viewModel.selectRankingView(RankingView.DETAILED)
        viewModel.selectCategory(com.brokechango.hattitriki.core.model.PlayerRankingCategory.MOST_WINS)

        assertEquals(RankingView.DETAILED, viewModel.uiState.value.rankingView)
    }
}
