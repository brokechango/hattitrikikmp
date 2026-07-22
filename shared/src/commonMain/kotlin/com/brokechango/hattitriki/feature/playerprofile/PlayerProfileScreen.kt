package com.brokechango.hattitriki.feature.playerprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.window.Dialog
import com.brokechango.hattitriki.core.design.CrestGold
import com.brokechango.hattitriki.core.design.CrestNavyLight
import com.brokechango.hattitriki.core.data.AvatarUpload
import com.brokechango.hattitriki.core.model.PlayerConnection
import com.brokechango.hattitriki.core.model.PlayerProfileSummary
import com.brokechango.hattitriki.core.model.PlayerRankingMetrics
import com.brokechango.hattitriki.core.model.Player
import com.brokechango.hattitriki.core.model.PlayerStats
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.ScreenTitle
import com.brokechango.hattitriki.ui.composables.SupabaseLoadingState
import com.brokechango.hattitriki.ui.preview.HattitrikiPreview
import com.brokechango.hattitriki.ui.preview.PreviewTargets
import com.github.panpf.sketch.AsyncImage
import hattitriki.shared.generated.resources.Res
import hattitriki.shared.generated.resources.icon_edit
import org.jetbrains.compose.resources.painterResource

@Composable
fun PlayerProfileScreen(
    viewModel: PlayerProfileViewModel,
    onPlayerSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val summary = uiState.summary
    val errorMessage = uiState.errorMessage

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        when {
            uiState.isLoading -> SupabaseLoadingState(message = "Cargando perfil del jugador…")
            errorMessage != null -> PlayerProfileError(
                message = errorMessage,
                onRetry = viewModel::refresh
            )
            summary != null -> PlayerProfileContent(
                summary = summary,
                avatarUrl = uiState.metadata?.avatarUrl,
                isCurrentPlayer = uiState.metadata?.isCurrentPlayer == true,
                hasLinkedAccount = uiState.metadata?.hasLinkedAccount == true,
                avatarWarning = uiState.avatarWarning,
                isUploadingAvatar = uiState.isUploadingAvatar,
                avatarFeedbackMessage = uiState.avatarFeedbackMessage,
                avatarErrorMessage = uiState.avatarErrorMessage,
                onAvatarSelected = viewModel::uploadAvatar,
                onAvatarPickerFailure = viewModel::reportAvatarPickerFailure,
                onPlayerSelected = onPlayerSelected
            )
        }
    }
}

@Composable
private fun PlayerProfileContent(
    summary: PlayerProfileSummary,
    avatarUrl: String?,
    isCurrentPlayer: Boolean,
    hasLinkedAccount: Boolean,
    avatarWarning: String?,
    isUploadingAvatar: Boolean,
    avatarFeedbackMessage: String?,
    avatarErrorMessage: String?,
    onAvatarSelected: (AvatarUpload) -> Unit,
    onAvatarPickerFailure: (String) -> Unit,
    onPlayerSelected: (String) -> Unit
) {
    val player = summary.stats.player
    var avatarDialog by rememberSaveable { mutableStateOf<AvatarDialog?>(null) }

    ScreenTitle(title = "Perfil")
    FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerAvatar(
                name = player.name,
                avatarUrl = avatarUrl,
                size = 92.dp,
                onClick = { avatarDialog = AvatarDialog.ACTIONS }
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = if (player.isActive) "JUGADOR ACTIVO" else "JUGADOR INACTIVO",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (player.isActive) CrestGold else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                if (isCurrentPlayer) {
                    Text(
                        text = "ESTE ERES TÚ",
                        style = MaterialTheme.typography.labelSmall,
                        color = CrestGold,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
    if (!hasLinkedAccount) {
        ProfileHint("Este jugador todavía no tiene una cuenta vinculada.")
    }
    avatarWarning?.let { warning ->
        ProfileHint(warning)
    }
    StatisticsCard(summary)
    RankingMetricsCard(summary)
    Text(
        text = "CONEXIONES EN LA LIGA",
        style = MaterialTheme.typography.labelLarge,
        color = CrestGold,
        fontWeight = FontWeight.Black
    )
    ConnectionCard(
        title = "Máximo rival",
        description = "El jugador contra el que más te has enfrentado.",
        connection = summary.maximumRival,
        color = Color(0xFFC95050),
        onPlayerSelected = onPlayerSelected
    )
    ConnectionCard(
        title = "Compañero inseparable",
        description = "El jugador con el que más has compartido equipo.",
        connection = summary.inseparableTeammate,
        color = Color(0xFF45B978),
        onPlayerSelected = onPlayerSelected
    )

    when (avatarDialog) {
        AvatarDialog.ACTIONS -> AvatarActionsDialog(
            name = player.name,
            canEdit = isCurrentPlayer,
            onViewPhoto = { avatarDialog = AvatarDialog.PREVIEW },
            onEditPhoto = { avatarDialog = AvatarDialog.EDIT },
            onDismiss = { avatarDialog = null }
        )
        AvatarDialog.PREVIEW -> AvatarPreviewDialog(
            name = player.name,
            avatarUrl = avatarUrl,
            onDismiss = { avatarDialog = null }
        )
        AvatarDialog.EDIT -> AvatarEditDialog(
            isUploading = isUploadingAvatar,
            successMessage = avatarFeedbackMessage,
            errorMessage = avatarErrorMessage,
            onAvatarSelected = onAvatarSelected,
            onPickerFailure = onAvatarPickerFailure,
            onDismiss = { avatarDialog = null }
        )
        null -> Unit
    }
}

@Composable
private fun PlayerAvatar(
    name: String,
    avatarUrl: String?,
    size: androidx.compose.ui.unit.Dp = 84.dp,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .size(size)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                    style = MaterialTheme.typography.headlineSmall,
                    color = CrestGold,
                    fontWeight = FontWeight.Black
                )
            }
        }
        if (onClick != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp),
                shape = CircleShape,
                color = CrestGold,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shadowElevation = 2.dp
            ) {
                Icon(
                    painter = painterResource(Res.drawable.icon_edit),
                    contentDescription = "Editar foto de perfil",
                    modifier = Modifier.padding(7.dp).size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

private enum class AvatarDialog {
    ACTIONS,
    PREVIEW,
    EDIT
}

@Composable
private fun AvatarActionsDialog(
    name: String,
    canEdit: Boolean,
    onViewPhoto: () -> Unit,
    onEditPhoto: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        AvatarDialogSurface {
            Text(
                text = "FOTO DE PERFIL",
                style = MaterialTheme.typography.labelLarge,
                color = CrestGold,
                fontWeight = FontWeight.Black
            )
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Elige qué quieres hacer con la foto.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onViewPhoto, modifier = Modifier.fillMaxWidth()) {
                Text("Ver foto")
            }
            if (canEdit) {
                OutlinedButton(onClick = onEditPhoto, modifier = Modifier.fillMaxWidth()) {
                    Text("Editar foto")
                }
            }
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar")
            }
        }
    }
}

@Composable
private fun AvatarPreviewDialog(
    name: String,
    avatarUrl: String?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        AvatarDialogSurface {
            Text(
                text = "FOTO DE PERFIL",
                style = MaterialTheme.typography.labelLarge,
                color = CrestGold,
                fontWeight = FontWeight.Black
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CrestNavyLight),
                contentAlignment = Alignment.Center
            ) {
                if (avatarUrl != null) {
                    AsyncImage(
                        uri = avatarUrl,
                        contentDescription = "Foto de perfil ampliada de $name",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    PlayerAvatar(name = name, avatarUrl = null, size = 152.dp)
                }
            }
            Text(
                text = if (avatarUrl == null) "Todavía no hay una foto subida." else name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar")
            }
        }
    }
}

@Composable
private fun AvatarEditDialog(
    isUploading: Boolean,
    successMessage: String?,
    errorMessage: String?,
    onAvatarSelected: (AvatarUpload) -> Unit,
    onPickerFailure: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val picker = rememberAvatarPicker(onAvatarSelected, onPickerFailure)

    Dialog(onDismissRequest = onDismiss) {
        AvatarDialogSurface {
            Text(
                text = "EDITAR FOTO",
                style = MaterialTheme.typography.labelLarge,
                color = CrestGold,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Actualiza tu foto de perfil",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "La foto se recortará en formato cuadrado y sólo será visible para la liga.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = picker::choosePhoto,
                enabled = !isUploading && !picker.isPreparing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        isUploading -> "Guardando foto…"
                        picker.isPreparing -> "Preparando foto…"
                        else -> "Elegir otra foto"
                    }
                )
            }
            Text(
                text = "JPEG o WebP · máximo 2,5 MB",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            successMessage?.let { message ->
                Text(message, color = Color(0xFF45B978), fontWeight = FontWeight.SemiBold)
            }
            errorMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error)
            }
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar")
            }
        }
    }
}

@Composable
private fun AvatarDialogSurface(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CrestGold.copy(alpha = 0.72f)),
        shadowElevation = 18.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 340.dp)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
internal fun AvatarUploadControl(
    isUploading: Boolean,
    successMessage: String?,
    errorMessage: String?,
    onAvatarSelected: (AvatarUpload) -> Unit,
    onPickerFailure: (String) -> Unit
) {
    val picker = rememberAvatarPicker(onAvatarSelected, onPickerFailure)

    FootballCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "TU FOTO",
                style = MaterialTheme.typography.labelLarge,
                color = CrestGold,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Elige una foto y la optimizaremos al formato del perfil antes de guardarla.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = picker::choosePhoto,
                enabled = !isUploading && !picker.isPreparing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        isUploading -> "Guardando foto…"
                        picker.isPreparing -> "Preparando foto…"
                        else -> "Cambiar foto de perfil"
                    }
                )
            }
            Text(
                text = "JPEG o WebP · máximo 2,5 MB · visible sólo para la liga",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            successMessage?.let { message ->
                Text(message, color = Color(0xFF45B978), fontWeight = FontWeight.SemiBold)
            }
            errorMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ProfileHint(message: String) {
    FootballCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PlayerProfileError(message: String, onRetry: () -> Unit) {
    FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("No se ha podido abrir el perfil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(message, color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun StatisticsCard(summary: PlayerProfileSummary) {
    val stats = summary.stats
    FootballCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "ESTADÍSTICAS",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CrestNavyLight.copy(alpha = 0.62f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black
            )
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatValue("PJ", stats.matchesPlayed.toString(), Modifier.weight(1f))
                StatValue("GOLES", stats.goals.toString(), Modifier.weight(1f), CrestGold)
                StatValue("PORTERO", stats.goalkeeperMatches.toString(), Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatValue("VICTORIAS", stats.wins.toString(), Modifier.weight(1f), Color(0xFF45B978))
                StatValue("EMPATES", stats.draws.toString(), Modifier.weight(1f))
                StatValue("DERROTAS", stats.losses.toString(), Modifier.weight(1f), Color(0xFFC95050))
            }
        }
    }
}

@Composable
private fun RankingMetricsCard(summary: PlayerProfileSummary) {
    val stats = summary.stats
    val metrics = summary.rankingMetrics
    FootballCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "MÉTRICAS DE RANKINGS",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CrestNavyLight.copy(alpha = 0.62f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black
            )
            RankingMetricRow(
                metrics = listOf(
                    RankingMetric("GOLES", stats.goals.toString(), CrestGold),
                    RankingMetric("G/P", formatRankingDecimal(metrics.goalsPerMatch), CrestGold),
                    RankingMetric("PJ", stats.matchesPlayed.toString())
                ),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            RankingMetricRow(
                metrics = listOf(
                    RankingMetric("GC", metrics.goalsAgainst?.let(::formatRankingGoalTotal) ?: "—"),
                    RankingMetric("GC/P", metrics.goalsAgainstPerMatch?.let(::formatRankingDecimal) ?: "—"),
                    RankingMetric("VICTORIAS", stats.wins.toString(), Color(0xFF45B978))
                ),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            RankingMetricRow(
                metrics = listOf(RankingMetric("TOTAL", metrics.totalPerformance.toString(), CrestGold)),
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 14.dp)
            )
        }
    }
}

private data class RankingMetric(
    val label: String,
    val value: String,
    val color: Color? = null
)

@Composable
private fun RankingMetricRow(metrics: List<RankingMetric>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (metrics.size == 1) {
            Spacer(modifier = Modifier.weight(1f))
        }
        metrics.forEach { metric ->
            StatValue(
                metric.label,
                metric.value,
                Modifier.weight(1f),
                metric.color ?: MaterialTheme.colorScheme.onSurface
            )
        }
        repeat(3 - metrics.size) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatValue(label: String, value: String, modifier: Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(
        modifier = modifier.heightIn(min = 54.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Black)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatRankingDecimal(value: Double): String =
    (kotlin.math.round(value * 10) / 10).toString()

private fun formatRankingGoalTotal(value: Double): String {
    val roundedValue = kotlin.math.round(value * 10) / 10
    return if (roundedValue % 1.0 == 0.0) roundedValue.toInt().toString() else roundedValue.toString()
}

@Composable
private fun ConnectionCard(
    title: String,
    description: String,
    connection: PlayerConnection?,
    color: Color,
    onPlayerSelected: (String) -> Unit
) {
    FootballCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (connection != null) Modifier.clickable { onPlayerSelected(connection.player.id) }
                else Modifier
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = color)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (connection == null) {
                Text("Aún no hay suficientes partidos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        connection.player.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${connection.matches} PJ  ›",
                        color = color,
                        fontWeight = FontWeight.Black
                    )
                }
                if (connection.isTied) {
                    Text("Empata con otro jugador.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun String.initials(): String =
    trim().split(Regex("\\s+")).filter(String::isNotBlank).take(2).joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }

@PreviewTargets
@Composable
private fun AvatarUploadControlPreview() {
    HattitrikiPreview {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            AvatarUploadControl(
                isUploading = false,
                successMessage = "Foto actualizada correctamente.",
                errorMessage = null,
                onAvatarSelected = {},
                onPickerFailure = {}
            )
        }
    }
}

@PreviewTargets
@Composable
private fun PlayerProfileScreenPreview() {
    val player = Player("1", "Arturo García")
    val summary = PlayerProfileSummary(
        stats = PlayerStats(
            player = player,
            matchesPlayed = 12,
            wins = 8,
            draws = 2,
            losses = 2,
            goals = 15,
            goalkeeperMatches = 1
        ),
        rankingMetrics = PlayerRankingMetrics(
            goalsPerMatch = 1.3,
            goalsAgainst = 2.0,
            goalsAgainstPerMatch = 2.0,
            assignedGoalsAgainst = 2,
            totalPerformance = 38
        ),
        maximumRival = PlayerConnection(Player("2", "Marta"), matches = 8, isTied = false),
        inseparableTeammate = PlayerConnection(Player("3", "Nico"), matches = 9, isTied = false)
    )
    HattitrikiPreview {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PlayerProfileContent(
                summary = summary,
                avatarUrl = null,
                isCurrentPlayer = true,
                hasLinkedAccount = true,
                avatarWarning = null,
                isUploadingAvatar = false,
                avatarFeedbackMessage = null,
                avatarErrorMessage = null,
                onAvatarSelected = {},
                onAvatarPickerFailure = {},
                onPlayerSelected = {}
            )
        }
    }
}
