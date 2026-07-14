package com.brokechango.hattitriki.feature.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.ScreenTitle

@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle(
            title = "Zona mister",
            subtitle = "Gestiona partidos y altas cuando el acceso este conectado."
        )

        FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Acceso de escritura", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = if (uiState.isAdmin) {
                        "Sesion admin activa."
                    } else {
                        "Pendiente de conectar Firebase Auth. Solo tu usuario podra editar."
                    }
                )
                Button(
                    onClick = { viewModel.onEvent(AdminEvent.LoginClicked) },
                    enabled = uiState.pendingFirebaseSetup
                ) {
                    Text("Conectar login")
                }
            }
        }

        OutlinedButton(
            onClick = { viewModel.onEvent(AdminEvent.NewMatchClicked) },
            enabled = uiState.isAdmin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nuevo partido")
        }

        OutlinedButton(
            onClick = { viewModel.onEvent(AdminEvent.AddPlayerClicked) },
            enabled = uiState.isAdmin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Anadir jugador")
        }
    }
}
