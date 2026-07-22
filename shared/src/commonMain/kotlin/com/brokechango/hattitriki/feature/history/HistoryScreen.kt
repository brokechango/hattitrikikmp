package com.brokechango.hattitriki.feature.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokechango.hattitriki.core.model.FriendlyMatch
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.HattitrikiPullToRefresh
import com.brokechango.hattitriki.ui.composables.PenaltyScore
import com.brokechango.hattitriki.ui.composables.ScorePill
import com.brokechango.hattitriki.ui.composables.ScreenTitle
import com.brokechango.hattitriki.ui.composables.SupabaseLoadingState
import hattitriki.shared.generated.resources.Res
import hattitriki.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onEvent: (HistoryEvent) -> Unit,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HattitrikiPullToRefresh(
        isRefreshing = uiState.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = modifier
    ) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val columns = if (maxWidth >= 900.dp) 2 else 1
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        ScreenTitle(
            title = stringResource(Res.string.history_title),
            subtitle = stringResource(Res.string.history_subtitle)
        )
        if (uiState.isLoading) {
            SupabaseLoadingState(message = stringResource(Res.string.history_loading))
            return@Column
        }
        uiState.errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
            return@Column
        }
        Text(
            text = stringResource(Res.string.history_finished_matches),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black
        )
        if (uiState.matches.isEmpty()) {
            Text(
                stringResource(Res.string.history_no_matches),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        uiState.matches.chunked(columns).forEach { matchRow ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                matchRow.forEach { match ->
                    HistoryMatchCard(
                        match = match,
                        onClick = { onEvent(HistoryEvent.OpenMatch(match.id)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(columns - matchRow.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
    }
    }
}

@Composable
private fun HistoryMatchCard(
    match: FriendlyMatch,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FootballCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    match.dateLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(Res.string.history_final),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(stringResource(Res.string.team_a), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                ScorePill(score = "${match.teamAScore} - ${match.teamBScore}")
                Text(
                    stringResource(Res.string.team_b),
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "",
                    style = MaterialTheme.typography.labelMedium,
                    minLines = 1
                )
                match.penaltyShootout?.let {
                    PenaltyScore(
                        score = "${it.teamAScore} - ${it.teamBScore}",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
