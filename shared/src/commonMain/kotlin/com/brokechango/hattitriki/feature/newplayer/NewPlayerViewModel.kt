package com.brokechango.hattitriki.feature.newplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brokechango.hattitriki.core.data.AdminPlayerRepository
import com.brokechango.hattitriki.core.data.AdminPlayersResult
import com.brokechango.hattitriki.core.data.CreatePlayerResult
import com.brokechango.hattitriki.core.data.EditPlayerResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewPlayerViewModel(
    private val repository: AdminPlayerRepository?,
    private val playerId: String? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewPlayerUiState(editingPlayerId = playerId))
    val uiState: StateFlow<NewPlayerUiState> = _uiState.asStateFlow()

    init {
        checkAccess()
    }

    fun onEvent(event: NewPlayerEvent) {
        when (event) {
            is NewPlayerEvent.NameChanged -> _uiState.value = _uiState.value.copy(
                name = event.value,
                errorMessage = null
            )
            is NewPlayerEvent.HasCardioChanged -> _uiState.value = _uiState.value.copy(
                hasCardio = event.value,
                errorMessage = null
            )
            NewPlayerEvent.Submit -> save()
        }
    }

    private fun checkAccess() {
        val playerRepository = repository
        if (playerRepository == null) {
            _uiState.value = _uiState.value.copy(
                isCheckingAccess = false,
                errorMessage = "Falta la configuración local de Supabase en este dispositivo."
            )
            return
        }

        viewModelScope.launch {
            if (!playerRepository.hasActiveAdminSession()) {
                _uiState.value = _uiState.value.copy(isCheckingAccess = false)
                return@launch
            }
            if (playerId == null) {
                _uiState.value = _uiState.value.copy(isCheckingAccess = false, isAdmin = true)
                return@launch
            }
            when (val result = playerRepository.loadPlayers()) {
                is AdminPlayersResult.Success -> {
                    val player = result.players.firstOrNull { it.id == playerId }
                    _uiState.value = _uiState.value.copy(
                        isCheckingAccess = false,
                        isAdmin = true,
                        name = player?.name.orEmpty(),
                        hasCardio = player?.hasCardio ?: false,
                        errorMessage = if (player == null) "No se ha encontrado el jugador." else null
                    )
                }
                AdminPlayersResult.Unauthorized -> _uiState.value = _uiState.value.copy(isCheckingAccess = false)
                is AdminPlayersResult.Failure -> _uiState.value = _uiState.value.copy(
                    isCheckingAccess = false,
                    isAdmin = true,
                    errorMessage = result.message
                )
            }
        }
    }

    private fun save() {
        val currentState = _uiState.value
        val playerRepository = repository ?: return
        if (!currentState.canSubmit) {
            _uiState.value = currentState.copy(errorMessage = "Indica el nombre del jugador.")
            return
        }

        _uiState.value = currentState.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            val result = currentState.editingPlayerId?.let {
                playerRepository.updatePlayer(it, currentState.name, currentState.hasCardio)
            } ?: when (val createResult = playerRepository.createPlayer(currentState.name, currentState.hasCardio)) {
                    CreatePlayerResult.Success -> EditPlayerResult.Success
                    CreatePlayerResult.Unauthorized -> EditPlayerResult.Unauthorized
                    is CreatePlayerResult.Failure -> EditPlayerResult.Failure(createResult.message)
                }
            when (result) {
                EditPlayerResult.Success -> _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isSaved = true
                )
                EditPlayerResult.Unauthorized -> _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isAdmin = false,
                    errorMessage = "Tu sesión ya no tiene permisos de administrador."
                )
                is EditPlayerResult.Failure -> _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = result.message
                )
            }
        }
    }
}
