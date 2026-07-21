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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.ScreenTitle

@Composable
fun AdminScreen(
    appVersion: String,
    onNewMatch: () -> Unit,
    onAddPlayer: () -> Unit,
    onManageMatches: () -> Unit,
    onManagePlayers: () -> Unit,
    onTeamRandomizer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ScreenTitle(
            title = "Zona míster",
            subtitle = "Gestiona partidos y jugadores desde un espacio privado."
        )

        FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Herramientas de administración",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Tu cuenta tiene permisos de míster. Elige qué quieres gestionar.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onNewMatch,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nuevo partido")
                }
                OutlinedButton(
                    onClick = onManageMatches,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Editar o borrar partidos")
                }
                OutlinedButton(
                    onClick = onAddPlayer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Añadir jugador")
                }
                OutlinedButton(
                    onClick = onManagePlayers,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Editar o borrar jugadores")
                }
                OutlinedButton(
                    onClick = onTeamRandomizer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generador de equipos")
                }
            }
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
