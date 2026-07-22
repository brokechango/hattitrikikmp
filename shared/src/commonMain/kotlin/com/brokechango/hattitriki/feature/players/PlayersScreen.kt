package com.brokechango.hattitriki.feature.players

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokechango.hattitriki.getPlatform
import com.brokechango.hattitriki.core.design.CrestGold
import com.brokechango.hattitriki.core.design.CrestNavyLight
import com.brokechango.hattitriki.core.design.CrestRed
import com.brokechango.hattitriki.core.model.Player
import com.brokechango.hattitriki.core.model.PlayerRankingCategory
import com.brokechango.hattitriki.core.model.PlayerStats
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.ScreenTitle
import com.brokechango.hattitriki.ui.composables.SupabaseLoadingState
import com.brokechango.hattitriki.ui.icons.rankingEmojiResource
import com.brokechango.hattitriki.ui.preview.HattitrikiPreview
import com.brokechango.hattitriki.ui.preview.PreviewTargets
import com.github.panpf.sketch.AsyncImage
import org.jetbrains.compose.resources.painterResource

@Composable
fun PlayersScreen(
    viewModel: PlayersViewModel,
    openTime: Long,
    onEvent: (PlayersEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categories = PlayerRankingCategory.entries
    var areFiltersVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(openTime) {
        viewModel.refresh()
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val useExpandedFilters = maxWidth >= 900.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        ScreenTitle(
            title = "Rankings"
        )
        if (uiState.isLoading) {
            SupabaseLoadingState(message = "Cargando los rankings…")
            return@Column
        }
        uiState.errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
            return@Column
        }
        RankingSummary(
            category = uiState.selectedCategory,
            playerCount = uiState.rankings.size
        )
        if (!useExpandedFilters) {
            FiltersToggle(
                areFiltersVisible = areFiltersVisible,
                onClick = { areFiltersVisible = !areFiltersVisible }
            )
        }
        if (useExpandedFilters || areFiltersVisible) {
            CategoryFilters(
                categories = categories,
                selectedCategory = uiState.selectedCategory,
                columns = if (useExpandedFilters) 4 else 2,
                onCategorySelected = { category -> onEvent(PlayersEvent.SelectCategory(category)) }
            )
        }
        RankingViewSelector(
            selectedView = uiState.rankingView,
            onViewSelected = { view -> onEvent(PlayersEvent.SelectRankingView(view)) }
        )
        RankingTable(
            category = uiState.selectedCategory,
            rankings = uiState.rankings,
            avatarUrlsByPlayerId = uiState.avatarUrlsByPlayerId,
            showRecentForm = uiState.rankingView == RankingView.DETAILED,
            onPlayerSelected = { playerId -> onEvent(PlayersEvent.SelectPlayer(playerId)) },
            modifier = Modifier.fillMaxWidth()
        )
    }
    }
}

@Composable
private fun FiltersToggle(
    areFiltersVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = if (areFiltersVisible) "Ocultar filtros" else "Mostrar filtros",
            fontWeight = FontWeight.Bold
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
                Image(
                    painter = painterResource(category.rankingEmojiResource),
                    contentDescription = category.title,
                    modifier = Modifier.size(28.dp)
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
    columns: Int,
    onCategorySelected: (PlayerRankingCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.chunked(columns).forEach { categoryRow ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryRow.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        modifier = Modifier.weight(1f),
                        label = {
                            Text(
                                text = category.title,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
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
                repeat(columns - categoryRow.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RankingTable(
    category: PlayerRankingCategory,
    rankings: List<PlayerRankingEntry>,
    avatarUrlsByPlayerId: Map<String, String>,
    showRecentForm: Boolean,
    onPlayerSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FootballCard(modifier = modifier) {
        BoxWithConstraints {
            val showInlineRecentForm = showRecentForm &&
                getPlatform().name == "Web"
            Column(modifier = Modifier.fillMaxWidth()) {
                RankingTableHeader(
                    category = category,
                    showInlineRecentForm = showInlineRecentForm
                )
                rankings.forEachIndexed { index, ranking ->
                    RankingRow(
                        index = index,
                        ranking = ranking,
                        avatarUrl = avatarUrlsByPlayerId[ranking.stats.player.id],
                        category = category,
                        showRecentForm = showRecentForm,
                        showInlineRecentForm = showInlineRecentForm,
                        onClick = { onPlayerSelected(ranking.stats.player.id) }
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
}

@Composable
private fun RankingTableHeader(
    category: PlayerRankingCategory,
    showInlineRecentForm: Boolean
) {
    val columns = rankingTableColumns(category)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CrestNavyLight.copy(alpha = 0.62f))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableLabel("#", Modifier.width(30.dp), TextAlign.Center)
        Spacer(modifier = Modifier.width(42.dp))
        TableLabel("JUGADOR", Modifier.weight(1f), TextAlign.Start)
        columns.forEach { column ->
            TableLabel(column.label, Modifier.width(column.width), column.textAlign)
        }
        if (showInlineRecentForm) {
            TableLabel("RACHA", Modifier.width(168.dp), TextAlign.Start)
        }
    }
}

@Composable
private fun RankingRow(
    index: Int,
    ranking: PlayerRankingEntry,
    avatarUrl: String?,
    category: PlayerRankingCategory,
    showRecentForm: Boolean,
    showInlineRecentForm: Boolean,
    onClick: () -> Unit
) {
    val stats = ranking.stats
    val rank = index + 1
    val columns = rankingTableColumns(category)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (rank <= 3) CrestGold.copy(alpha = 0.06f) else Color.Transparent
            )
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RankNumber(rank = rank, modifier = Modifier.width(30.dp))
            RankingPlayerAvatar(
                name = stats.player.name,
                avatarUrl = avatarUrl
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stats.player.name,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (rank <= 3) FontWeight.Black else FontWeight.Bold
            )
            columns.forEach { column ->
                TableValue(
                    text = column.value(ranking),
                    modifier = Modifier.width(column.width),
                    textAlign = column.textAlign,
                    color = if (column.isPrimaryMetric) CrestGold else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (showInlineRecentForm) {
                RecentForm(
                    results = ranking.recentForm,
                    modifier = Modifier.width(168.dp).padding(end = 12.dp)
                )
            }
        }
        if (showRecentForm && !showInlineRecentForm) {
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
private fun RankingPlayerAvatar(name: String, avatarUrl: String?) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(CrestNavyLight),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl != null) {
            AsyncImage(
                uri = avatarUrl,
                contentDescription = "Foto de perfil de $name",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = name.initials(),
                style = MaterialTheme.typography.labelMedium,
                color = CrestGold,
                fontWeight = FontWeight.Black
            )
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
    PlayerMatchResult.DID_NOT_PLAY -> Color(0xFF8A949E)
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
    textAlign: TextAlign = TextAlign.Center,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Text(
        text = text,
        modifier = modifier,
        textAlign = textAlign,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        fontWeight = FontWeight.Bold
    )
}

private data class RankingTableColumn(
    val label: String,
    val width: Dp,
    val textAlign: TextAlign = TextAlign.Center,
    val isPrimaryMetric: Boolean = false,
    val value: (PlayerRankingEntry) -> String
)

private fun rankingTableColumns(category: PlayerRankingCategory): List<RankingTableColumn> {
    fun support(label: String, width: Dp = 28.dp, value: (PlayerRankingEntry) -> String) =
        RankingTableColumn(label = label, width = width, value = value)
    fun metric(label: String, value: (PlayerRankingEntry) -> String) = RankingTableColumn(
        label = label,
        width = 54.dp,
        textAlign = TextAlign.End,
        isPrimaryMetric = true,
        value = value
    )

    return when (category) {
        PlayerRankingCategory.TOP_SCORER -> listOf(
            support("PJ", 30.dp) { it.stats.matchesPlayed.toString() },
            support("G/P", 36.dp) { it.goalsPerMatch() },
            metric("G") { it.value }
        )
        PlayerRankingCategory.GOALS_PER_MATCH -> listOf(
            support("PJ", 30.dp) { it.stats.matchesPlayed.toString() },
            support("G") { it.stats.goals.toString() },
            metric("G/P") { it.value }
        )
        PlayerRankingCategory.ZAMORA -> listOf(
            support("PP", 30.dp) { it.stats.goalkeeperMatches.toString() },
            support("GC/P", 36.dp) { it.goalsAgainstPerGoalkeeperMatch() },
            metric("GC") { it.value }
        )
        PlayerRankingCategory.GOALS_CONCEDED_PER_MATCH -> listOf(
            support("PP", 30.dp) { it.stats.goalkeeperMatches.toString() },
            support("GC") { it.formattedGoalsAgainst() },
            metric("GC/P") { it.value }
        )
        PlayerRankingCategory.MOST_PLAYED -> listOf(
            support("V") { it.stats.wins.toString() },
            support("E") { it.stats.draws.toString() },
            support("D") { it.stats.losses.toString() },
            metric("PJ") { it.value }
        )
        PlayerRankingCategory.MOST_WINS -> listOf(
            support("PJ", 30.dp) { it.stats.matchesPlayed.toString() },
            support("E") { it.stats.draws.toString() },
            support("D") { it.stats.losses.toString() },
            metric("V") { it.value }
        )
        PlayerRankingCategory.PLAYER_ON_FORM -> listOf(
            support("PJ", 30.dp) { it.stats.matchesPlayed.toString() },
            support("G") { it.stats.goals.toString() },
            support("GC") { it.formattedGoalsAgainst() },
            support("V") { it.stats.wins.toString() },
            metric("TOTAL") { it.value }
        )
    }
}

private fun PlayerRankingEntry.goalsPerMatch(): String =
    formatRankingDecimal(stats.goals.toDouble() / stats.matchesPlayed.coerceAtLeast(1))

private fun PlayerRankingEntry.goalsAgainstPerGoalkeeperMatch(): String = goalsAgainst?.let { goalsAgainst ->
    formatRankingDecimal(goalsAgainst / stats.goalkeeperMatches.coerceAtLeast(1))
} ?: "—"

private fun PlayerRankingEntry.formattedGoalsAgainst(): String = goalsAgainst?.let(::formatRankingGoalTotal) ?: "—"

private fun formatRankingDecimal(value: Double): String =
    (kotlin.math.round(value * 10) / 10).toString()

private fun formatRankingGoalTotal(value: Double): String {
    val roundedValue = kotlin.math.round(value * 10) / 10
    return if (roundedValue % 1.0 == 0.0) roundedValue.toInt().toString() else roundedValue.toString()
}

private fun rankingMetricLabel(category: PlayerRankingCategory): String =
    rankingTableColumns(category).last().label

private fun String.initials(): String =
    trim().split(Regex("\\s+")).filter(String::isNotBlank).take(2).joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }

@PreviewTargets
@Composable
private fun PlayersScreenPreview() {
    val category = PlayerRankingCategory.TOP_SCORER
    val rankings = listOf(
        PlayerRankingEntry(
            stats = PlayerStats(Player("1", "Arturo"), 12, 8, 2, 2, 15, 0),
            value = "15",
            recentForm = listOf(PlayerMatchResult.WIN, PlayerMatchResult.WIN, PlayerMatchResult.DRAW),
            goalsAgainst = null
        ),
        PlayerRankingEntry(
            stats = PlayerStats(Player("2", "Marta"), 11, 6, 2, 3, 11, 0),
            value = "11",
            recentForm = listOf(PlayerMatchResult.LOSS, PlayerMatchResult.WIN, PlayerMatchResult.WIN),
            goalsAgainst = null
        )
    )

    HattitrikiPreview {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useExpandedFilters = maxWidth >= 900.dp
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ScreenTitle(title = "Rankings")
                RankingSummary(category = category, playerCount = rankings.size)
                if (useExpandedFilters) {
                    CategoryFilters(
                        categories = PlayerRankingCategory.entries,
                        selectedCategory = category,
                        columns = 4,
                        onCategorySelected = {}
                    )
                } else {
                    FiltersToggle(areFiltersVisible = false, onClick = {})
                }
                RankingViewSelector(selectedView = RankingView.DETAILED, onViewSelected = {})
                RankingTable(
                    category = category,
                    rankings = rankings,
                    avatarUrlsByPlayerId = emptyMap(),
                    showRecentForm = true,
                    onPlayerSelected = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
