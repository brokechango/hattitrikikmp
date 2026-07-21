package com.brokechango.hattitriki.feature.newplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.ScreenTitle
import com.brokechango.hattitriki.ui.composables.SupabaseLoadingState

@Composable
fun NewPlayerScreen(
    viewModel: NewPlayerViewModel,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle(
            title = if (uiState.isEditing) "Editar jugador" else "Añadir jugador",
            subtitle = if (uiState.isEditing) "Actualiza el nombre del jugador." else "Da de alta un jugador para la liga."
        )

        when {
            uiState.isCheckingAccess -> SupabaseLoadingState(
                message = "Comprobando permisos…",
                compact = true
            )
            !uiState.isAdmin -> AccessDenied(uiState.errorMessage)
            else -> NewPlayerForm(uiState, viewModel::onEvent)
        }
    }
}

@Composable
private fun AccessDenied(errorMessage: String?) {
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
private fun NewPlayerForm(
    uiState: NewPlayerUiState,
    onEvent: (NewPlayerEvent) -> Unit
) {
    FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { onEvent(NewPlayerEvent.NameChanged(it)) },
                label = { Text("Nombre del jugador") },
                singleLine = true,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.hasCardio,
                    onCheckedChange = { onEvent(NewPlayerEvent.HasCardioChanged(it)) },
                    enabled = !uiState.isSaving
                )
                Text("Está en buena forma física")
            }

            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = { onEvent(NewPlayerEvent.Submit) },
                enabled = uiState.canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        uiState.isSaving && uiState.isEditing -> "Actualizando…"
                        uiState.isSaving -> "Guardando…"
                        uiState.isEditing -> "Guardar cambios"
                        else -> "Guardar jugador"
                    }
                )
            }
        }
    }
}
