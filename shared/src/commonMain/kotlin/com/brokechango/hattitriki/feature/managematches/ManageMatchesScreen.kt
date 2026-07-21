package com.brokechango.hattitriki.feature.managematches

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
import com.brokechango.hattitriki.core.data.AdminMatchRepository
import com.brokechango.hattitriki.core.data.AdminMatchSummary
import com.brokechango.hattitriki.core.data.AdminMatchesResult
import com.brokechango.hattitriki.core.data.EditMatchResult
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.ScreenTitle
import com.brokechango.hattitriki.ui.composables.SupabaseLoadingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ManageMatchesUiState(
    val isLoading: Boolean = true,
    val isAdmin: Boolean = false,
    val matches: List<AdminMatchSummary> = emptyList(),
    val matchPendingDeletion: AdminMatchSummary? = null,
    val errorMessage: String? = null
)

class ManageMatchesViewModel(private val repository: AdminMatchRepository?) : ViewModel() {
    private val _uiState = MutableStateFlow(ManageMatchesUiState())
    val uiState: StateFlow<ManageMatchesUiState> = _uiState.asStateFlow()

    init { load() }

    fun requestDelete(match: AdminMatchSummary) {
        _uiState.value = _uiState.value.copy(matchPendingDeletion = match, errorMessage = null)
    }

    fun dismissDelete() { _uiState.value = _uiState.value.copy(matchPendingDeletion = null) }

    fun confirmDelete() {
        val match = _uiState.value.matchPendingDeletion ?: return
        val matchRepository = repository ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = matchRepository.deleteMatch(match.id)) {
                EditMatchResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    matches = _uiState.value.matches.filterNot { it.id == match.id },
                    matchPendingDeletion = null
                )
                EditMatchResult.Unauthorized -> _uiState.value = _uiState.value.copy(
                    isLoading = false, isAdmin = false, matchPendingDeletion = null,
                    errorMessage = "Tu sesión ya no tiene permisos de administrador."
                )
                is EditMatchResult.Failure -> _uiState.value = _uiState.value.copy(
                    isLoading = false, matchPendingDeletion = null, errorMessage = result.message
                )
            }
        }
    }

    private fun load() {
        val matchRepository = repository
        if (matchRepository == null) {
            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Falta la configuración local de Supabase en este dispositivo.")
            return
        }
        viewModelScope.launch {
            when (val result = matchRepository.loadMatches()) {
                is AdminMatchesResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false, isAdmin = true, matches = result.matches
                )
                AdminMatchesResult.Unauthorized -> _uiState.value = _uiState.value.copy(isLoading = false)
                is AdminMatchesResult.Failure -> _uiState.value = _uiState.value.copy(
                    isLoading = false, isAdmin = true, errorMessage = result.message
                )
            }
        }
    }
}

@Composable
fun ManageMatchesScreen(
    viewModel: ManageMatchesViewModel,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenTitle(
            title = "Gestionar partidos",
            subtitle = "Edita o borra actas y sus datos asociados."
        )
        when {
            uiState.isLoading -> SupabaseLoadingState(
                message = "Cargando los partidos…",
                compact = true
            )
            !uiState.isAdmin -> Text(uiState.errorMessage ?: "Inicia sesión como administrador desde la Zona míster.")
            else -> {
                uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (uiState.matches.isEmpty()) Text("No hay partidos para gestionar.")
                uiState.matches.forEach { match ->
                    FootballCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(match.playedOn, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Equipo A ${match.teamAScore} - ${match.teamBScore} Equipo B" +
                                    match.penaltyShootoutLabel
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(onClick = { onEdit(match.id) }, modifier = Modifier.weight(1f)) { Text("Editar") }
                                TextButton(onClick = { viewModel.requestDelete(match) }) { Text("Borrar") }
                            }
                        }
                    }
                }
            }
        }
    }
    uiState.matchPendingDeletion?.let { match ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text("Borrar partido") },
            text = { Text("¿Quieres borrar el acta del ${match.playedOn}? También se borrarán sus alineaciones y goles.") },
            confirmButton = { Button(onClick = viewModel::confirmDelete) { Text("Borrar") } },
            dismissButton = { TextButton(onClick = viewModel::dismissDelete) { Text("Cancelar") } }
        )
    }
}

private val AdminMatchSummary.penaltyShootoutLabel: String
    get() = if (teamAPenaltyScore != null && teamBPenaltyScore != null) {
        " · $teamAPenaltyScore - $teamBPenaltyScore en penaltis"
    } else {
        ""
    }
