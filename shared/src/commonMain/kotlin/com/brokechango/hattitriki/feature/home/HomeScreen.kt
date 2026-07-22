package com.brokechango.hattitriki.feature.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokechango.hattitriki.core.design.CrestGold
import com.brokechango.hattitriki.core.design.CrestGoldLight
import com.brokechango.hattitriki.core.model.FriendlyMatch
import com.brokechango.hattitriki.core.model.PlayerRankingCategory
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.HattitrikiPullToRefresh
import com.brokechango.hattitriki.ui.composables.PenaltyScore
import com.brokechango.hattitriki.ui.composables.ScorePill
import com.brokechango.hattitriki.ui.composables.ScreenTitle
import com.brokechango.hattitriki.ui.composables.SupabaseLoadingState
import com.brokechango.hattitriki.ui.icons.rankingEmojiResource
import com.brokechango.hattitriki.ui.preview.HattitrikiPreview
import com.brokechango.hattitriki.ui.preview.PreviewTargets
import com.github.panpf.sketch.AsyncImage
import hattitriki.shared.generated.resources.Res
import hattitriki.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onEvent: (HomeEvent) -> Unit,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HattitrikiPullToRefresh(
        isRefreshing = uiState.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = modifier
    ) {
    Column(
        modifier = Modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle(
            title = stringResource(Res.string.home_title),
            subtitle = stringResource(Res.string.home_subtitle)
        )

        if (uiState.isLoading) {
            HomeLoadingState()
            return@Column
        }
        uiState.errorMessage?.let { message ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(message, color = MaterialTheme.colorScheme.error)
                Button(onClick = viewModel::refresh) {
                    Text(stringResource(Res.string.action_retry))
                }
            }
            return@Column
        }

        uiState.latestMatch?.let { match ->
            FootballCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEvent(HomeEvent.OpenMatch(match.id)) },
                highlight = true
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(Res.string.home_last_result),
                            style = MaterialTheme.typography.labelMedium,
                            color = CrestGold,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            match.dateLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(Res.string.team_a), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        ScorePill(score = "${match.teamAScore} - ${match.teamBScore}")
                        Text(stringResource(Res.string.team_b), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    match.penaltyShootout?.let {
                        PenaltyScore(
                            score = "${it.teamAScore} - ${it.teamBScore}",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        if (uiState.latestMatch == null) {
            FootballCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.home_no_results),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stringResource(Res.string.home_no_results_description),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(top = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(Res.string.home_season),
                style = MaterialTheme.typography.labelMedium,
                color = CrestGold,
                fontWeight = FontWeight.Black
            )
            Text(
                text = stringResource(Res.string.home_league_stats),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                text = stringResource(Res.string.home_stats_summary, uiState.totalMatches, uiState.totalGoals),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        FeaturedStatsGrid(
            stats = uiState.featuredStats,
            avatarUrlsByPlayerId = uiState.avatarUrlsByPlayerId,
            onOpenRanking = { category -> onEvent(HomeEvent.OpenPlayers(category)) },
            onOrderChanged = viewModel::updateFeaturedStatsOrder
        )
    }
    }
}

@Composable
private fun HomeLoadingState(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= 700.dp) 3 else 2
        val placeholders = 6
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SupabaseLoadingState(
                message = stringResource(Res.string.home_loading),
                compact = true
            )
            FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.24f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CrestGold.copy(alpha = 0.3f))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.62f)
                            .height(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                    )
                }
            }
            repeat((placeholders + columns - 1) / columns) { rowIndex ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(columns) { columnIndex ->
                        val itemIndex = rowIndex * columns + columnIndex
                        if (itemIndex < placeholders) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(if (columns == 3) 118.dp else 132.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                            )
                        } else {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedStatsGrid(
    stats: List<HomeFeaturedStat>,
    avatarUrlsByPlayerId: Map<String, String>,
    onOpenRanking: (PlayerRankingCategory) -> Unit,
    onOrderChanged: (List<PlayerRankingCategory>) -> Unit,
    modifier: Modifier = Modifier
) {
    val orderedStats = remember {
        mutableStateListOf<HomeFeaturedStat>().apply { addAll(stats) }
    }
    var draggedCategory by remember { mutableStateOf<PlayerRankingCategory?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(stats) {
        orderedStats.clear()
        orderedStats.addAll(stats)
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = when {
            maxWidth >= 700.dp -> 3
            else -> 2
        }
        val spacing = 12.dp
        val cardWidth = (maxWidth - spacing * (columns - 1)) / columns
        val cardHeight = if (maxWidth >= 700.dp) 154.dp else cardWidth
        val rows = (orderedStats.size + columns - 1) / columns
        val gridHeight = if (rows == 0) 0.dp else {
            cardHeight * rows + spacing * (rows - 1)
        }
        val density = LocalDensity.current
        val cardWidthPx = with(density) { cardWidth.toPx() }
        val cardHeightPx = with(density) { cardHeight.toPx() }
        val horizontalStepPx = with(density) { (cardWidth + spacing).toPx() }
        val verticalStepPx = with(density) { (cardHeight + spacing).toPx() }

        fun slotOffset(index: Int): Offset = Offset(
            x = (index % columns) * horizontalStepPx,
            y = (index / columns) * verticalStepPx
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight)
        ) {
            orderedStats.forEachIndexed { index, stat ->
                key(stat.category) {
                    val targetX = (cardWidth + spacing) * (index % columns)
                    val targetY = (cardHeight + spacing) * (index / columns)
                    val animatedX by animateDpAsState(
                        targetValue = targetX,
                        animationSpec = tween(durationMillis = 180),
                        label = "${stat.category.name} x"
                    )
                    val animatedY by animateDpAsState(
                        targetValue = targetY,
                        animationSpec = tween(durationMillis = 180),
                        label = "${stat.category.name} y"
                    )
                    val isDragging = draggedCategory == stat.category
                    val isReordering = draggedCategory != null
                    val scale by animateFloatAsState(
                        targetValue = if (isDragging) 1.04f else 1f,
                        animationSpec = tween(durationMillis = 110),
                        label = "${stat.category.name} scale"
                    )
                    val rotation = remember(stat.category) { Animatable(0f) }

                    LaunchedEffect(isReordering, isDragging) {
                        if (isReordering && !isDragging) {
                            val direction = if (stat.category.ordinal % 2 == 0) 1f else -1f
                            while (true) {
                                rotation.animateTo(
                                    targetValue = 1.15f * direction,
                                    animationSpec = tween(durationMillis = 95)
                                )
                                rotation.animateTo(
                                    targetValue = -1.15f * direction,
                                    animationSpec = tween(durationMillis = 130)
                                )
                            }
                        } else {
                            rotation.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = 90)
                            )
                        }
                    }

                    FeaturedStatCard(
                        stat = stat,
                        avatarUrl = avatarUrlsByPlayerId[stat.playerId],
                        onClick = { onOpenRanking(stat.category) },
                        modifier = Modifier
                            .offset {
                                if (isDragging) {
                                    val slot = slotOffset(index)
                                    IntOffset(
                                        x = (slot.x + dragOffset.x).roundToInt(),
                                        y = (slot.y + dragOffset.y).roundToInt()
                                    )
                                } else {
                                    IntOffset(
                                        x = with(density) { animatedX.roundToPx() },
                                        y = with(density) { animatedY.roundToPx() }
                                    )
                                }
                            }
                            .size(width = cardWidth, height = cardHeight)
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                rotationZ = rotation.value
                            }
                            .pointerInput(
                                stat.category,
                                columns,
                                cardWidthPx,
                                cardHeightPx,
                                horizontalStepPx,
                                verticalStepPx
                            ) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggedCategory = stat.category
                                        dragOffset = Offset.Zero
                                        hapticFeedback.performHapticFeedback(
                                            HapticFeedbackType.LongPress
                                        )
                                    },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        val currentIndex = orderedStats.indexOfFirst {
                                            it.category == stat.category
                                        }
                                        if (currentIndex == -1) return@detectDragGesturesAfterLongPress

                                        val oldSlot = slotOffset(currentIndex)
                                        val nextDragOffset = dragOffset + amount
                                        val draggedTopLeft = oldSlot + nextDragOffset
                                        val targetColumn = (draggedTopLeft.x / horizontalStepPx)
                                            .roundToInt()
                                            .coerceIn(0, columns - 1)
                                        val targetRow = (draggedTopLeft.y / verticalStepPx)
                                            .roundToInt()
                                            .coerceIn(0, rows - 1)
                                        val targetIndex = (targetRow * columns + targetColumn)
                                            .coerceIn(0, orderedStats.lastIndex)

                                        if (targetIndex == currentIndex) {
                                            dragOffset = nextDragOffset
                                        } else {
                                            val newSlot = slotOffset(targetIndex)
                                            val movedStat = orderedStats.removeAt(currentIndex)
                                            orderedStats.add(targetIndex, movedStat)
                                            dragOffset = nextDragOffset + oldSlot - newSlot
                                            onOrderChanged(orderedStats.map { it.category })
                                        }
                                    },
                                    onDragEnd = {
                                        draggedCategory = null
                                        dragOffset = Offset.Zero
                                    },
                                    onDragCancel = {
                                        draggedCategory = null
                                        dragOffset = Offset.Zero
                                    }
                                )
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedStatCard(
    stat: HomeFeaturedStat,
    avatarUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FootballCard(
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            FeaturedPlayerBackground(avatarUrl = avatarUrl)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(CrestGold)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = stat.category.title,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    StatIcon(category = stat.category)
                }
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = stat.playerName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CrestGold.copy(alpha = 0.16f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = stat.value,
                            style = MaterialTheme.typography.headlineSmall,
                            color = CrestGoldLight,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = stat.category.detail,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedPlayerBackground(
    avatarUrl: String?
) {
    if (avatarUrl == null) return

    AsyncImage(
        uri = avatarUrl,
        contentDescription = null,
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.56f)
            .graphicsLayer(alpha = 0.18f),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun StatIcon(
    category: PlayerRankingCategory,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(CrestGold.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(category.rankingEmojiResource),
            contentDescription = category.title,
            modifier = Modifier.size(22.dp)
        )
    }
}

@PreviewTargets
@Composable
private fun HomeScreenPreview() {
    val state = HomeUiState(
        latestMatch = FriendlyMatch(
            id = "match-1",
            dateLabel = "22 jul 2026",
            teamAScore = 4,
            teamBScore = 3,
            players = emptyList(),
            goals = emptyList()
        ),
        totalMatches = 12,
        totalGoals = 73,
        featuredStats = listOf(
            HomeFeaturedStat(PlayerRankingCategory.TOP_SCORER, "1", "Arturo", "15"),
            HomeFeaturedStat(PlayerRankingCategory.GOALS_PER_MATCH, "2", "Marta", "1,8"),
            HomeFeaturedStat(PlayerRankingCategory.ZAMORA, "3", "Nico", "1,1"),
            HomeFeaturedStat(PlayerRankingCategory.MOST_PLAYED, "4", "Laura", "12")
        ),
        isLoading = false
    )

    HattitrikiPreview {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenTitle(
                title = "Hattitriki FC",
                subtitle = "Resultados y estadísticas de la liga"
            )
            FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("ÚLTIMO RESULTADO", color = CrestGold, fontWeight = FontWeight.Black)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Equipo A", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        ScorePill(score = "${state.latestMatch?.teamAScore} - ${state.latestMatch?.teamBScore}")
                        Text("Equipo B", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("TEMPORADA", color = CrestGold, fontWeight = FontWeight.Black)
                Text("Estadísticas de la liga", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text(
                    "${state.totalMatches} partidos · ${state.totalGoals} goles",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FeaturedStatsGrid(
                stats = state.featuredStats,
                avatarUrlsByPlayerId = emptyMap(),
                onOpenRanking = {},
                onOrderChanged = {}
            )
        }
    }
}
