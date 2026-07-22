package com.brokechango.hattitriki.feature.playerprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokechango.hattitriki.core.design.CrestGold
import com.brokechango.hattitriki.core.design.CrestNavyLight
import com.brokechango.hattitriki.core.data.AvatarUpload
import com.brokechango.hattitriki.core.model.PlayerConnection
import com.brokechango.hattitriki.core.model.PlayerProfileSummary
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.ScreenTitle
import com.brokechango.hattitriki.ui.composables.SupabaseLoadingState
import com.github.panpf.sketch.AsyncImage

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
    ScreenTitle(
        title = player.name,
        subtitle = if (isCurrentPlayer) "Tu perfil de jugador" else "Perfil de jugador"
    )
    FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerAvatar(name = player.name, avatarUrl = avatarUrl, size = 92.dp)
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
    if (isCurrentPlayer) {
        AvatarChangeControl(
            isUploading = isUploadingAvatar,
            successMessage = avatarFeedbackMessage,
            errorMessage = avatarErrorMessage,
            onAvatarSelected = onAvatarSelected,
            onPickerFailure = onAvatarPickerFailure
        )
    } else if (!hasLinkedAccount) {
        ProfileHint("Este jugador todavía no tiene una cuenta vinculada.")
    }
    avatarWarning?.let { warning ->
        ProfileHint(warning)
    }
    StatisticsCard(summary)
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
}

@Composable
private fun PlayerAvatar(name: String, avatarUrl: String?, size: androidx.compose.ui.unit.Dp = 84.dp) {
    Box(
        modifier = Modifier
            .size(size)
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
}

@Composable
private fun AvatarChangeControl(
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
                text = "JPEG o WebP · máximo 300 KB · visible sólo para la liga",
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
