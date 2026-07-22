package com.brokechango.hattitriki.feature.invitation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokechango.hattitriki.core.data.AdminPlayer
import com.brokechango.hattitriki.core.design.CrestGold
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.ScreenTitle
import com.brokechango.hattitriki.ui.composables.SupabaseLoadingState

@Composable
fun LeagueInvitationScreen(
    viewModel: LeagueInvitationViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle(
            title = "Invitar y vincular",
            subtitle = "Da acceso a un jugador existente sin perder ni duplicar su historial."
        )

        when {
            uiState.isCheckingAccess -> SupabaseLoadingState(
                message = "Comprobando permisos…",
                compact = true
            )
            !uiState.isAdmin -> InvitationAccessDenied(uiState.errorMessage)
            else -> InvitationForm(uiState, viewModel::onEvent)
        }
    }
}

@Composable
private fun InvitationAccessDenied(errorMessage: String?) {
    FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Acceso restringido", style = MaterialTheme.typography.titleMedium)
            Text(errorMessage ?: "Inicia sesión como administrador desde la Zona míster.")
        }
    }
}

@Composable
private fun InvitationForm(
    uiState: LeagueInvitationUiState,
    onEvent: (LeagueInvitationEvent) -> Unit
) {
    FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "1 · Elige al jugador",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Sólo aparecen jugadores activos sin una cuenta ni una invitación pendiente.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            PlayerSelector(uiState, onEvent)
            uiState.playersErrorMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error)
                OutlinedButton(
                    onClick = { onEvent(LeagueInvitationEvent.RetryPlayers) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver a cargar jugadores")
                }
            }
            uiState.selectedPlayer?.let { player ->
                Text(
                    text = "La cuenta de ${player.name} conservará todas sus estadísticas actuales y futuras.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { onEvent(LeagueInvitationEvent.EmailChanged(it)) },
                label = { Text("2 · Correo electrónico") },
                singleLine = true,
                enabled = !uiState.isSending,
                isError = uiState.emailError != null,
                supportingText = uiState.emailError?.let { message -> { Text(message) } },
                modifier = Modifier.fillMaxWidth()
            )

            uiState.errorMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error)
            }

            uiState.sentEmail?.let { email ->
                Text(
                    text = "Invitación enviada a $email.",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = { onEvent(LeagueInvitationEvent.Submit) },
                enabled = uiState.canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isSending) "Enviando invitación…" else "Enviar acceso a la liga")
            }

            if (uiState.sentEmail != null) {
                OutlinedButton(
                    onClick = { onEvent(LeagueInvitationEvent.SendAnother) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Invitar a otra persona")
                }
            }
        }
    }
}

@Composable
private fun PlayerSelector(
    uiState: LeagueInvitationUiState,
    onEvent: (LeagueInvitationEvent) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val selectedPlayer = uiState.selectedPlayer
    val selectorEnabled = !uiState.isSending && !uiState.isLoadingPlayers && uiState.players.isNotEmpty()

    OutlinedButton(
        onClick = { expanded = true },
        enabled = selectorEnabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selectedPlayer != null) CrestGold.copy(alpha = 0.84f) else colors.outline.copy(alpha = 0.68f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selectedPlayer != null) CrestGold.copy(alpha = 0.10f) else colors.surface,
            contentColor = colors.onSurface
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerBadge(player = selectedPlayer)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (selectedPlayer != null) "JUGADOR SELECCIONADO" else "JUGADOR DE LA LIGA",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedPlayer != null) colors.primary else colors.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when {
                        uiState.isLoadingPlayers -> "Cargando jugadores…"
                        selectedPlayer != null -> selectedPlayer.name
                        uiState.playersErrorMessage != null -> "No se han podido cargar los jugadores"
                        uiState.players.isEmpty() -> "No quedan jugadores disponibles"
                        else -> "Selecciona un jugador"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (selectorEnabled) {
                    Text(
                        text = "${uiState.players.size} disponible${if (uiState.players.size == 1) "" else "s"} para vincular",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
            Text(
                text = if (expanded) "⌃" else "⌄",
                style = MaterialTheme.typography.titleMedium,
                color = if (selectorEnabled) colors.primary else colors.onSurfaceVariant
            )
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth().heightIn(max = 304.dp),
            shape = RoundedCornerShape(14.dp),
            containerColor = colors.surface,
            tonalElevation = 6.dp,
            shadowElevation = 10.dp
        ) {
            Text(
                text = "JUGADORES DISPONIBLES",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.7f))
            uiState.players.forEach { player ->
                DropdownMenuItem(
                    text = {
                        PlayerMenuRow(
                            player = player,
                            isSelected = player.id == selectedPlayer?.id
                        )
                    },
                    onClick = {
                        onEvent(LeagueInvitationEvent.PlayerSelected(player.id))
                        expanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun PlayerBadge(player: AdminPlayer?) {
    val colors = MaterialTheme.colorScheme
    val initials = player?.name
        ?.trim()
        ?.split(Regex("\\s+"))
        ?.take(2)
        ?.joinToString("") { part -> part.take(1).uppercase() }
        ?.ifBlank { "?" }
        ?: "?"

    Surface(
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        color = if (player == null) colors.surfaceVariant else colors.primary.copy(alpha = 0.16f),
        contentColor = if (player == null) colors.onSurfaceVariant else colors.primary
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(initials, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PlayerMenuRow(player: AdminPlayer, isSelected: Boolean) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerBadge(player)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = player.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Disponible para vincular",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )
        }
        if (isSelected) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = CrestGold.copy(alpha = 0.16f),
                contentColor = colors.primary
            ) {
                Text(
                    text = "ELEGIDO",
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
