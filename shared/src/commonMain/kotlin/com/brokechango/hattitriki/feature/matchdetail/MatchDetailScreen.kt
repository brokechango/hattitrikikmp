package com.brokechango.hattitriki.feature.matchdetail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokechango.hattitriki.core.design.CrestGold
import com.brokechango.hattitriki.core.design.CrestRed
import com.brokechango.hattitriki.core.model.GoalEntry
import com.brokechango.hattitriki.core.model.TeamSide
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.ScorePill
import com.brokechango.hattitriki.ui.composables.ScreenTitle

@Composable
fun MatchDetailScreen(
    viewModel: MatchDetailViewModel,
    onEvent: (MatchDetailEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val match = uiState.match

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (uiState.isLoading) {
            Text("Cargando acta…")
            return@Column
        }
        uiState.errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
            return@Column
        }
        if (match == null) {
            Text("No se encontro el partido.")
            return@Column
        }

        ScreenTitle(
            title = match.dateLabel,
            subtitle = "Acta del partido y alineaciones."
        )
        MatchScoreboard(
            teamAScore = match.teamAScore,
            teamBScore = match.teamBScore,
            penaltyShootout = match.penaltyShootout
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TeamCard(
                title = "Equipo A",
                team = TeamSide.A,
                uiState = uiState,
                modifier = Modifier.weight(1f)
            )
            TeamCard(
                title = "Equipo B",
                team = TeamSide.B,
                uiState = uiState,
                modifier = Modifier.weight(1f)
            )
        }

        GoalsSummary(uiState = uiState)
    }
}

@Composable
private fun MatchScoreboard(
    teamAScore: Int,
    teamBScore: Int,
    penaltyShootout: com.brokechango.hattitriki.core.model.PenaltyShootout?
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Equipo A",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            ScorePill("$teamAScore  -  $teamBScore")
            Text(
                text = "Equipo B",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
        }
        penaltyShootout?.let { shootout ->
            Text(
                "Equipo ${shootout.winner.name} gana ${shootout.teamAScore} - ${shootout.teamBScore} en penaltis",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = CrestGold,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GoalsSummary(uiState: MatchDetailUiState) {
    val match = uiState.match ?: return

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Goles",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "EQUIPO A",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "  GOL  ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "EQUIPO B",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
        FootballCard {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                match.goals.forEach { goal ->
                    GoalRow(
                        goal = goal,
                        scorerName = uiState.playersById[goal.playerId]?.name.orEmpty(),
                        goalkeeperWhoConceded = uiState.playersById[goal.goalkeeperId]?.name.orEmpty()
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalRow(
    goal: GoalEntry,
    scorerName: String,
    goalkeeperWhoConceded: String
) {
    val teamAScored = goal.team == TeamSide.A

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (goal.isOwnGoal) CrestRed.copy(alpha = 0.30f) else Color.Transparent)
            .height(42.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (teamAScored) {
            GoalPlayer(
                goal = goal,
                name = scorerName,
                alignment = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        } else {
            GoalkeeperWhoConceded(
                name = goalkeeperWhoConceded,
                isOwnGoal = goal.isOwnGoal,
                alignment = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
        GoalMarker(isOwnGoal = goal.isOwnGoal)
        if (teamAScored) {
            GoalkeeperWhoConceded(
                name = goalkeeperWhoConceded,
                isOwnGoal = goal.isOwnGoal,
                alignment = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )
        } else {
            GoalPlayer(
                goal = goal,
                name = scorerName,
                alignment = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GoalPlayer(
    goal: GoalEntry,
    name: String,
    alignment: TextAlign,
    modifier: Modifier = Modifier
) {
    Text(
        text = "$name  ·  ${goal.count}",
        modifier = modifier.padding(horizontal = 12.dp),
        textAlign = alignment,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun GoalkeeperWhoConceded(
    name: String,
    isOwnGoal: Boolean,
    alignment: TextAlign,
    modifier: Modifier = Modifier
) {
    if (name.isBlank()) {
        Box(modifier = modifier)
        return
    }

    Row(
        modifier = modifier.padding(horizontal = 12.dp),
        horizontalArrangement = if (alignment == TextAlign.End) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (alignment == TextAlign.End) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            GoalkeeperGlove(
                color = if (isOwnGoal) CrestRed else CrestGold,
                modifier = Modifier.padding(start = 6.dp)
            )
        } else {
            GoalkeeperGlove(
                color = if (isOwnGoal) CrestRed else CrestGold,
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun GoalMarker(isOwnGoal: Boolean) {
    Box(
        modifier = Modifier
            .height(42.dp)
            .drawBehind {
                drawLine(
                    color = CrestGold.copy(alpha = 0.35f),
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isOwnGoal) "O.G" else "⚽",
            style = MaterialTheme.typography.labelMedium,
            color = if (isOwnGoal) CrestRed else CrestGold,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TeamCard(
    title: String,
    team: TeamSide,
    uiState: MatchDetailUiState,
    modifier: Modifier = Modifier
) {
    val match = uiState.match ?: return

    FootballCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            match.players.filter { it.team == team }.forEach { matchPlayer ->
                val name = uiState.playersById[matchPlayer.playerId]?.name.orEmpty()
                PlayerLine(name = name, isGoalkeeper = matchPlayer.wasGoalkeeper)
            }
        }
    }
}

@Composable
private fun PlayerLine(name: String, isGoalkeeper: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (isGoalkeeper) {
            GoalkeeperGlove()
        }
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isGoalkeeper) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun GoalkeeperGlove(
    color: Color = CrestGold,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(18.dp)
            .semantics { contentDescription = "Portero" }
    ) {
        val fingerWidth = size.width * 0.16f
        val fingerHeight = size.height * 0.47f
        val fingerTop = size.height * 0.06f
        val corner = CornerRadius(fingerWidth / 2, fingerWidth / 2)

        listOf(0.18f, 0.37f, 0.56f).forEach { x ->
            drawRoundRect(
                color = color,
                topLeft = Offset(size.width * x, fingerTop),
                size = Size(fingerWidth, fingerHeight),
                cornerRadius = corner
            )
        }
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.18f, size.height * 0.37f),
            size = Size(size.width * 0.58f, size.height * 0.39f),
            cornerRadius = CornerRadius(size.width * 0.12f, size.width * 0.12f)
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.30f, size.height * 0.76f),
            size = Size(size.width * 0.42f, size.height * 0.18f),
            cornerRadius = CornerRadius(size.width * 0.05f, size.width * 0.05f)
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.05f, size.height * 0.42f),
            size = Size(size.width * 0.24f, size.height * 0.19f),
            cornerRadius = CornerRadius(size.width * 0.08f, size.width * 0.08f)
        )
    }
}
