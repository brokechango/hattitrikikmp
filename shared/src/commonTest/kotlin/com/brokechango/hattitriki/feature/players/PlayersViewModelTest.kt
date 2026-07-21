package com.brokechango.hattitriki.feature.players

import com.brokechango.hattitriki.core.data.FootballSnapshot
import com.brokechango.hattitriki.core.model.FriendlyMatch
import com.brokechango.hattitriki.core.model.GoalEntry
import com.brokechango.hattitriki.core.model.MatchPlayer
import com.brokechango.hattitriki.core.model.Player
import com.brokechango.hattitriki.core.model.PlayerRankingCategory
import com.brokechango.hattitriki.core.model.TeamSide
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayersViewModelTest {

    @Test
    fun rankingViewIsCompactByDefault() {
        val viewModel = PlayersViewModel(initialSnapshot = leagueSnapshot())

        assertEquals(RankingView.COMPACT, viewModel.uiState.value.rankingView)
    }

    @Test
    fun detailedViewShowsEachPlayersFiveMostRecentResults() {
        val viewModel = PlayersViewModel(initialSnapshot = leagueSnapshot())

        viewModel.selectRankingView(RankingView.DETAILED)

        val alex = viewModel.uiState.value.rankings.first { it.stats.player.id == "alex" }
        assertEquals(RankingView.DETAILED, viewModel.uiState.value.rankingView)
        assertEquals(
            listOf(
                PlayerMatchResult.WIN,
                PlayerMatchResult.LOSS,
                PlayerMatchResult.WIN,
                PlayerMatchResult.WIN,
                PlayerMatchResult.LOSS
            ),
            alex.recentForm
        )
    }

    @Test
    fun changingCategoryKeepsTheChosenRankingView() {
        val viewModel = PlayersViewModel(initialSnapshot = leagueSnapshot())

        viewModel.selectRankingView(RankingView.DETAILED)
        viewModel.selectCategory(com.brokechango.hattitriki.core.model.PlayerRankingCategory.MOST_WINS)

        assertEquals(RankingView.DETAILED, viewModel.uiState.value.rankingView)
    }

    @Test
    fun zamoraSplitsTeamGoalsAgainstBetweenEveryGoalkeeperInTheMatch() {
        val ana = Player(id = "ana", name = "Ana")
        val bruno = Player(id = "bruno", name = "Bruno")
        val carlos = Player(id = "carlos", name = "Carlos")
        val alex = Player(id = "alex", name = "Alex")
        val snapshot = FootballSnapshot(
            players = listOf(ana, bruno, carlos, alex),
            matches = listOf(
                FriendlyMatch(
                    id = "1",
                    dateLabel = "1",
                    teamAScore = 3,
                    teamBScore = 0,
                    players = listOf(
                        MatchPlayer(ana.id, TeamSide.A, wasGoalkeeper = true),
                        MatchPlayer(alex.id, TeamSide.A),
                        MatchPlayer(bruno.id, TeamSide.B, wasGoalkeeper = true),
                        MatchPlayer(carlos.id, TeamSide.B, wasGoalkeeper = true)
                    ),
                    goals = listOf(
                        GoalEntry(alex.id, TeamSide.A, count = 2, goalkeeperId = bruno.id),
                        GoalEntry(alex.id, TeamSide.A, count = 1, goalkeeperId = carlos.id)
                    )
                )
            )
        )
        val viewModel = PlayersViewModel(initialSnapshot = snapshot)

        viewModel.selectCategory(PlayerRankingCategory.ZAMORA)

        assertEquals(
            listOf("ana" to "0", "bruno" to "1.5", "carlos" to "1.5"),
            viewModel.uiState.value.rankings.map { it.stats.player.id to it.value }
        )

        viewModel.selectCategory(PlayerRankingCategory.GOALS_CONCEDED_PER_MATCH)

        assertEquals(
            listOf("ana" to "0.0", "bruno" to "1.5", "carlos" to "1.5"),
            viewModel.uiState.value.rankings.map { it.stats.player.id to it.value }
        )
    }

    private fun leagueSnapshot(): FootballSnapshot {
        val alex = Player(id = "alex", name = "Alex")
        val bruno = Player(id = "bruno", name = "Bruno")
        return FootballSnapshot(
            players = listOf(alex, bruno),
            matches = listOf(
                match("5", 2, 1),
                match("4", 0, 1),
                match("3", 3, 1),
                match("2", 1, 0),
                match("1", 0, 2)
            )
        )
    }

    private fun match(id: String, teamAScore: Int, teamBScore: Int) = FriendlyMatch(
        id = id,
        dateLabel = id,
        teamAScore = teamAScore,
        teamBScore = teamBScore,
        players = listOf(
            MatchPlayer(playerId = "alex", team = TeamSide.A),
            MatchPlayer(playerId = "bruno", team = TeamSide.B)
        ),
        goals = emptyList()
    )
}
