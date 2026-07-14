package com.brokechango.hattitriki.feature.players

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokechango.hattitriki.core.design.CrestGold
import com.brokechango.hattitriki.core.design.CrestNavyLight
import com.brokechango.hattitriki.core.design.CrestRed
import com.brokechango.hattitriki.core.model.PlayerRankingCategory
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.ScreenTitle

@Composable
fun PlayersScreen(
    viewModel: PlayersViewModel,
    onEvent: (PlayersEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categories = PlayerRankingCategory.entries

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenTitle(
            title = "Clasificaciones",
            subtitle = ""
        )
        RankingSummary(
            category = uiState.selectedCategory,
            playerCount = uiState.rankings.size
        )
        CategoryFilters(
            categories = categories,
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = { category -> onEvent(PlayersEvent.SelectCategory(category)) }
        )
        RankingViewSelector(
            selectedView = uiState.rankingView,
            onViewSelected = { view -> onEvent(PlayersEvent.SelectRankingView(view)) }
        )
        RankingTable(
            category = uiState.selectedCategory,
            rankings = uiState.rankings,
            showRecentForm = uiState.rankingView == RankingView.DETAILED
        )
    }
}

@Composable
private fun RankingViewSelector(
    selectedView: RankingView,
    onViewSelected: (RankingView) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RankingView.entries.forEach { view ->
            FilterChip(
                selected = selectedView == view,
                onClick = { onViewSelected(view) },
                modifier = Modifier.weight(1f),
                label = {
                    Text(
                        text = view.label,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Black
                    )
                },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selectedView == view) CrestGold else MaterialTheme.colorScheme.outline
                ),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CrestGold,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun RankingSummary(
    category: PlayerRankingCategory,
    playerCount: Int,
    modifier: Modifier = Modifier
) {
    FootballCard(
        modifier = modifier.fillMaxWidth(),
        highlight = true
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CrestGold),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.icon,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "TEMPORADA · $playerCount JUGADORES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = rankingMetricLabel(category),
                color = CrestGold,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun CategoryFilters(
    categories: List<PlayerRankingCategory>,
    selectedCategory: PlayerRankingCategory,
    onCategorySelected: (PlayerRankingCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category.title,
                        fontWeight = FontWeight.Bold
                    )
                },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selectedCategory == category) CrestGold else MaterialTheme.colorScheme.outline
                ),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CrestGold,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun RankingTable(
    category: PlayerRankingCategory,
    rankings: List<PlayerRankingEntry>,
    showRecentForm: Boolean,
    modifier: Modifier = Modifier
) {
    FootballCard(modifier = modifier.fillMaxWidth()) {
        Column {
            RankingTableHeader(category = category)
            rankings.forEachIndexed { index, ranking ->
                RankingRow(
                    index = index,
                    ranking = ranking,
                    category = category,
                    showRecentForm = showRecentForm
                )
                if (index < rankings.lastIndex) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RankingTableHeader(category: PlayerRankingCategory) {
    val isPlayerOnForm = category == PlayerRankingCategory.PLAYER_ON_FORM
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CrestNavyLight.copy(alpha = 0.62f))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableLabel("#", Modifier.width(30.dp), TextAlign.Center)
        TableLabel("JUGADOR", Modifier.weight(1f), TextAlign.Start)
        TableLabel("PJ", Modifier.width(30.dp), TextAlign.Center)
        TableLabel(if (isPlayerOnForm) "G" else "V", Modifier.width(28.dp), TextAlign.Center)
        TableLabel(if (isPlayerOnForm) "GC" else "E", Modifier.width(28.dp), TextAlign.Center)
        TableLabel(if (isPlayerOnForm) "V" else "D", Modifier.width(28.dp), TextAlign.Center)
        TableLabel(rankingMetricLabel(category), Modifier.width(54.dp), TextAlign.End)
    }
}

@Composable
private fun RankingRow(
    index: Int,
    ranking: PlayerRankingEntry,
    category: PlayerRankingCategory,
    showRecentForm: Boolean
) {
    val stats = ranking.stats
    val rank = index + 1
    val isPlayerOnForm = category == PlayerRankingCategory.PLAYER_ON_FORM
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (rank <= 3) CrestGold.copy(alpha = 0.06f) else Color.Transparent
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RankNumber(rank = rank, modifier = Modifier.width(30.dp))
            Text(
                text = stats.player.name,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (rank <= 3) FontWeight.Black else FontWeight.Bold
            )
            TableValue(stats.matchesPlayed.toString(), Modifier.width(30.dp))
            if (isPlayerOnForm) {
                TableValue(stats.goals.toString(), Modifier.width(28.dp), color = CrestGold)
                TableValue(ranking.goalsAgainst?.toString() ?: "—", Modifier.width(28.dp))
                TableValue(stats.wins.toString(), Modifier.width(28.dp), color = CrestGold)
            } else {
                TableValue(stats.wins.toString(), Modifier.width(28.dp), color = CrestGold)
                TableValue(stats.draws.toString(), Modifier.width(28.dp))
                TableValue(stats.losses.toString(), Modifier.width(28.dp))
            }
            Text(
                text = ranking.value,
                modifier = Modifier.width(54.dp),
                textAlign = TextAlign.End,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge,
                color = CrestGold,
                fontWeight = FontWeight.Black
            )
        }
        if (showRecentForm) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            ) {
                Spacer(modifier = Modifier.width(30.dp))
                Spacer(modifier = Modifier.weight(1f))
                RecentForm(
                    results = ranking.recentForm,
                    modifier = Modifier.width(168.dp)
                )
            }
        }
    }
}

@Composable
private fun RecentForm(
    results: List<PlayerMatchResult>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "ÚLTIMOS 5",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Black
        )
        if (results.isEmpty()) {
            Text(
                text = "Sin partidos",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Row(
                modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                results.forEach { result ->
                    Box(
                        modifier = Modifier
                            .size(width = 20.dp, height = 20.dp)
                            .background(result.backgroundColor()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = result.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = result.contentColor(),
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

private fun PlayerMatchResult.backgroundColor(): Color = when (this) {
    PlayerMatchResult.WIN -> Color(0xFF45B978)
    PlayerMatchResult.DRAW -> Color(0xFFD1D7DE)
    PlayerMatchResult.LOSS -> CrestRed
}

private fun PlayerMatchResult.contentColor(): Color = when (this) {
    PlayerMatchResult.DRAW -> Color(0xFF16212D)
    else -> Color.White
}

@Composable
private fun RankNumber(rank: Int, modifier: Modifier = Modifier) {
    val color = when (rank) {
        1 -> CrestGold
        2 -> MaterialTheme.colorScheme.onSurface
        3 -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = rank.toString(),
        modifier = modifier,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        fontWeight = FontWeight.Black
    )
}

@Composable
private fun TableLabel(
    text: String,
    modifier: Modifier,
    textAlign: TextAlign
) {
    Text(
        text = text,
        modifier = modifier,
        textAlign = textAlign,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Black
    )
}

@Composable
private fun TableValue(
    text: String,
    modifier: Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Text(
        text = text,
        modifier = modifier,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        fontWeight = FontWeight.Bold
    )
}

private fun rankingMetricLabel(category: PlayerRankingCategory): String = when (category) {
    PlayerRankingCategory.TOP_SCORER -> "G"
    PlayerRankingCategory.GOALS_PER_MATCH -> "G/P"
    PlayerRankingCategory.ZAMORA -> "GC"
    PlayerRankingCategory.GOALS_CONCEDED_PER_MATCH -> "GC/P"
    PlayerRankingCategory.MOST_PLAYED -> "PJ"
    PlayerRankingCategory.MOST_WINS -> "V"
    PlayerRankingCategory.PLAYER_ON_FORM -> "TOTAL"
}
