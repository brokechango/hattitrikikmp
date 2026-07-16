package com.brokechango.hattitriki.feature.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.ScreenTitle

@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    appVersion: String,
    onNewMatch: () -> Unit,
    onAddPlayer: () -> Unit,
    onManageMatches: () -> Unit,
    onManagePlayers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle(
            title = "Zona mister",
            subtitle = "Gestiona partidos y altas con una cuenta administradora."
        )

        FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Acceso de escritura", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (uiState.isAdmin) {
                    Text("Sesión admin activa.")
                    OutlinedButton(
                        onClick = { viewModel.onEvent(AdminEvent.LogoutClicked) },
                        enabled = !uiState.isLoading
                    ) {
                        Text("Cerrar sesión")
                    }
                } else {
                    Text("Inicia sesión con tu cuenta administradora.")

                    if (!uiState.isAuthConfigured) {
                        Text(
                            text = "Falta la configuración local de Supabase en este dispositivo.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEvent(AdminEvent.EmailChanged(it)) },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onEvent(AdminEvent.PasswordChanged(it)) },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    uiState.errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Button(
                        onClick = { viewModel.onEvent(AdminEvent.SubmitLogin) },
                        enabled = uiState.canSubmitLogin,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (uiState.isLoading) "Entrando…" else "Entrar")
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onNewMatch,
            enabled = uiState.isAdmin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nuevo partido")
        }

        OutlinedButton(
            onClick = onManageMatches,
            enabled = uiState.isAdmin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Editar o borrar partidos")
        }

        OutlinedButton(
            onClick = onAddPlayer,
            enabled = uiState.isAdmin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Añadir jugador")
        }

        OutlinedButton(
            onClick = onManagePlayers,
            enabled = uiState.isAdmin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Editar o borrar jugadores")
        }

        Text(
            text = "Versión $appVersion",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
