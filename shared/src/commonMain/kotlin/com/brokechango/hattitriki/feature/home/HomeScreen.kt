package com.brokechango.hattitriki.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokechango.hattitriki.core.design.CrestGold
import com.brokechango.hattitriki.core.design.CrestGoldLight
import com.brokechango.hattitriki.core.model.PlayerRankingCategory
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.PenaltyScore
import com.brokechango.hattitriki.ui.composables.ScorePill
import com.brokechango.hattitriki.ui.composables.ScreenTitle

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle(
            title = "Liga Genuine",
            subtitle = "Resultados, rachas y campeones de la liga Genuine."
        )

        if (uiState.isLoading) {
            Text("Cargando datos de la liga…")
            return@Column
        }
        uiState.errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
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
                            "ÚLTIMO RESULTADO",
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
                        Text("Equipo A", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        ScorePill(score = "${match.teamAScore} - ${match.teamBScore}")
                        Text("Equipo B", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
            Text(
                "Todavía no hay partidos guardados.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            modifier = Modifier.padding(top = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "TEMPORADA",
                style = MaterialTheme.typography.labelMedium,
                color = CrestGold,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Estadísticas de la liga",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "${uiState.totalMatches} partidos · ${uiState.totalGoals} goles",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        FeaturedStatsGrid(
            stats = uiState.featuredStats,
            onOpenRanking = { category -> onEvent(HomeEvent.OpenPlayers(category)) }
        )
    }
}

@Composable
private fun FeaturedStatsGrid(
    stats: List<HomeFeaturedStat>,
    onOpenRanking: (PlayerRankingCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = when {
            maxWidth >= 900.dp -> 4
            maxWidth >= 620.dp -> 3
            else -> 2
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            stats.chunked(columns).forEach { rowStats ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowStats.forEach { stat ->
                        FeaturedStatCard(
                            stat = stat,
                            onClick = { onOpenRanking(stat.category) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(columns - rowStats.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedStatCard(
    stat: HomeFeaturedStat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FootballCard(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        highlight = stat.category == PlayerRankingCategory.TOP_SCORER
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                    StatIcon(icon = stat.category.icon)
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
private fun StatIcon(
    icon: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(CrestGold.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            color = Color.Unspecified,
            fontSize = 17.sp
        )
    }
}
