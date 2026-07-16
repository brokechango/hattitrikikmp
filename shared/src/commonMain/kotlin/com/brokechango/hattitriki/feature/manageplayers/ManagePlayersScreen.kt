package com.brokechango.hattitriki.feature.manageplayers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.brokechango.hattitriki.core.data.AdminPlayer
import com.brokechango.hattitriki.core.data.AdminPlayerRepository
import com.brokechango.hattitriki.core.data.AdminPlayersResult
import com.brokechango.hattitriki.core.data.EditPlayerResult
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.ScreenTitle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ManagePlayersUiState(
    val isLoading: Boolean = true,
    val isAdmin: Boolean = false,
    val players: List<AdminPlayer> = emptyList(),
    val playerPendingDeletion: AdminPlayer? = null,
    val errorMessage: String? = null
)

class ManagePlayersViewModel(private val repository: AdminPlayerRepository?) : ViewModel() {
    private val _uiState = MutableStateFlow(ManagePlayersUiState())
    val uiState: StateFlow<ManagePlayersUiState> = _uiState.asStateFlow()

    init { load() }

    fun requestDelete(player: AdminPlayer) {
        _uiState.value = _uiState.value.copy(playerPendingDeletion = player, errorMessage = null)
    }

    fun dismissDelete() {
        _uiState.value = _uiState.value.copy(playerPendingDeletion = null)
    }

    fun confirmDelete() {
        val player = _uiState.value.playerPendingDeletion ?: return
        val playerRepository = repository ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = playerRepository.deletePlayer(player.id)) {
                EditPlayerResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    players = _uiState.value.players.filterNot { it.id == player.id },
                    playerPendingDeletion = null
                )
                EditPlayerResult.Unauthorized -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAdmin = false,
                    playerPendingDeletion = null,
                    errorMessage = "Tu sesión ya no tiene permisos de administrador."
                )
                is EditPlayerResult.Failure -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    playerPendingDeletion = null,
                    errorMessage = result.message
                )
            }
        }
    }

    private fun load() {
        val playerRepository = repository
        if (playerRepository == null) {
            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Falta la configuración local de Supabase en este dispositivo.")
            return
        }
        viewModelScope.launch {
            when (val result = playerRepository.loadPlayers()) {
                is AdminPlayersResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false, isAdmin = true, players = result.players
                )
                AdminPlayersResult.Unauthorized -> _uiState.value = _uiState.value.copy(isLoading = false)
                is AdminPlayersResult.Failure -> _uiState.value = _uiState.value.copy(
                    isLoading = false, isAdmin = true, errorMessage = result.message
                )
            }
        }
    }
}

@Composable
fun ManagePlayersScreen(
    viewModel: ManagePlayersViewModel,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenTitle(
            title = "Gestionar jugadores",
            subtitle = "Edita nombres o borra jugadores sin historial."
        )
        when {
            uiState.isLoading -> Text("Cargando jugadores…")
            !uiState.isAdmin -> Text(uiState.errorMessage ?: "Inicia sesión como administrador desde la Zona míster.")
            else -> {
                uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (uiState.players.isEmpty()) Text("No hay jugadores para gestionar.")
                uiState.players.forEach { player ->
                    FootballCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(player.name, style = MaterialTheme.typography.titleMedium)
                            if (!player.isActive) Text("Inactivo", style = MaterialTheme.typography.labelMedium)
                            if (player.hasCardio) Text("En buena forma física", style = MaterialTheme.typography.labelMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(onClick = { onEdit(player.id) }, modifier = Modifier.weight(1f)) { Text("Editar") }
                                TextButton(onClick = { viewModel.requestDelete(player) }) { Text("Borrar") }
                            }
                        }
                    }
                }
            }
        }
    }
    uiState.playerPendingDeletion?.let { player ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text("Borrar jugador") },
            text = { Text("¿Quieres borrar a ${player.name}? Esta acción no se puede deshacer.") },
            confirmButton = { Button(onClick = viewModel::confirmDelete) { Text("Borrar") } },
            dismissButton = { TextButton(onClick = viewModel::dismissDelete) { Text("Cancelar") } }
        )
    }
}
