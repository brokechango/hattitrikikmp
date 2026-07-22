package com.brokechango.hattitriki.feature.invitation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brokechango.hattitriki.core.data.AdminPlayersResult
import com.brokechango.hattitriki.core.data.LeagueInvitationGateway
import com.brokechango.hattitriki.core.data.SendLeagueInvitationResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class LeagueInvitationViewModel(
    private val invitationGateway: LeagueInvitationGateway?
) : ViewModel() {
    private companion object {
        const val invitationSendTimeoutMillis = 20_000L
    }

    private val _uiState = MutableStateFlow(LeagueInvitationUiState())
    val uiState: StateFlow<LeagueInvitationUiState> = _uiState.asStateFlow()

    init {
        checkAccess()
    }

    fun onEvent(event: LeagueInvitationEvent) {
        when (event) {
            is LeagueInvitationEvent.EmailChanged -> _uiState.value = _uiState.value.copy(
                email = event.value,
                sentEmail = null,
                errorMessage = null
            )
            is LeagueInvitationEvent.PlayerSelected -> _uiState.value = _uiState.value.copy(
                selectedPlayerId = event.playerId,
                sentEmail = null,
                errorMessage = null
            )
            LeagueInvitationEvent.RetryPlayers -> invitationGateway?.let { gateway ->
                viewModelScope.launch { loadPlayers(gateway) }
            }
            LeagueInvitationEvent.Submit -> sendInvitation()
            LeagueInvitationEvent.SendAnother -> _uiState.value = _uiState.value.copy(
                email = "",
                selectedPlayerId = null,
                sentEmail = null,
                errorMessage = null
            )
        }
    }

    private fun checkAccess() {
        val gateway = invitationGateway
        if (gateway == null) {
            _uiState.value = _uiState.value.copy(
                isCheckingAccess = false,
                errorMessage = "Falta la configuración local de Supabase en este dispositivo."
            )
            return
        }

        viewModelScope.launch {
            val isAdmin = gateway.hasActiveAdminSession()
            _uiState.value = _uiState.value.copy(
                isCheckingAccess = false,
                isAdmin = isAdmin,
                errorMessage = if (isAdmin) null else "Tu sesión ya no tiene permisos de administrador."
            )
            if (isAdmin) loadPlayers(gateway)
        }
    }

    private suspend fun loadPlayers(gateway: LeagueInvitationGateway) {
        _uiState.value = _uiState.value.copy(isLoadingPlayers = true, playersErrorMessage = null)
        when (val result = gateway.loadPlayers()) {
            is AdminPlayersResult.Success -> _uiState.value = _uiState.value.copy(
                isLoadingPlayers = false,
                players = result.players.filter { it.isActive },
                playersErrorMessage = null
            )
            AdminPlayersResult.Unauthorized -> _uiState.value = _uiState.value.copy(
                isLoadingPlayers = false,
                isAdmin = false,
                playersErrorMessage = null,
                errorMessage = "Tu sesión ya no tiene permisos de administrador."
            )
            is AdminPlayersResult.Failure -> _uiState.value = _uiState.value.copy(
                isLoadingPlayers = false,
                playersErrorMessage = result.message
            )
        }
    }

    private fun sendInvitation() {
        val state = _uiState.value
        val gateway = invitationGateway ?: return
        if (!state.canSubmit) {
            _uiState.value = state.copy(
                errorMessage = if (state.selectedPlayer == null) {
                    "Selecciona el jugador que recibirá la cuenta."
                } else {
                    "Indica un correo electrónico válido."
                }
            )
            return
        }

        _uiState.value = state.copy(isSending = true, errorMessage = null, sentEmail = null)
        viewModelScope.launch {
            try {
                when (
                    val result = withTimeout(invitationSendTimeoutMillis) {
                        gateway.sendInvitation(checkNotNull(state.selectedPlayerId), state.normalizedEmail)
                    }
                ) {
                    is SendLeagueInvitationResult.Success -> _uiState.value = _uiState.value.copy(
                        email = "",
                        isSending = false,
                        sentEmail = result.email,
                        errorMessage = null
                    )
                    SendLeagueInvitationResult.Unauthorized -> _uiState.value = _uiState.value.copy(
                        isSending = false,
                        isAdmin = false,
                        errorMessage = "Tu sesión ya no tiene permisos para enviar invitaciones."
                    )
                    is SendLeagueInvitationResult.Failure -> _uiState.value = _uiState.value.copy(
                        isSending = false,
                        errorMessage = result.message
                    )
                }
            } catch (_: TimeoutCancellationException) {
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    errorMessage = "El envío está tardando demasiado. Comprueba tu conexión e inténtalo de nuevo."
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    errorMessage = "No se ha podido enviar la invitación. Inténtalo de nuevo."
                )
            }
        }
    }
}
