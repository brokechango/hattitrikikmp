package com.brokechango.hattitriki.feature.invitation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val selectedPlayerName = uiState.selectedPlayer?.name
    OutlinedButton(
        onClick = { expanded = true },
        enabled = !uiState.isSending && !uiState.isLoadingPlayers && uiState.players.isNotEmpty(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            when {
                uiState.isLoadingPlayers -> "Cargando jugadores…"
                selectedPlayerName != null -> selectedPlayerName
                uiState.playersErrorMessage != null -> "No se han podido cargar los jugadores"
                uiState.players.isEmpty() -> "Todos los jugadores activos ya tienen acceso"
                else -> "Selecciona un jugador"
            }
        )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        uiState.players.forEach { player ->
            DropdownMenuItem(
                text = { Text(player.name) },
                onClick = {
                    onEvent(LeagueInvitationEvent.PlayerSelected(player.id))
                    expanded = false
                }
            )
        }
    }
}
