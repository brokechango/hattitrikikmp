package com.brokechango.hattitriki.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brokechango.hattitriki.core.data.AvatarUpload
import com.brokechango.hattitriki.core.data.AvatarUploadResult
import com.brokechango.hattitriki.core.data.CurrentPlayerIdResult
import com.brokechango.hattitriki.core.data.FootballSnapshotResult
import com.brokechango.hattitriki.core.data.FriendlyFootballRepository
import com.brokechango.hattitriki.core.data.PlayerProfileMetadataResult
import com.brokechango.hattitriki.core.data.PlayerProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = true,
    val isUploadingAvatar: Boolean = false,
    val playerId: String? = null,
    val playerName: String? = null,
    val avatarUrl: String? = null,
    val avatarWarning: String? = null,
    val avatarFeedbackMessage: String? = null,
    val avatarErrorMessage: String? = null,
    val errorMessage: String? = null
)

/** Owns the account settings independently from a player profile opened in rankings. */
class SettingsViewModel(
    private val footballRepository: FriendlyFootballRepository?,
    private val profileRepository: PlayerProfileRepository?
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = SettingsUiState(isLoading = true)
        viewModelScope.launch {
            val leagueRepository = footballRepository
            val accountRepository = profileRepository
            if (leagueRepository == null || accountRepository == null) {
                _uiState.value = SettingsUiState(
                    isLoading = false,
                    errorMessage = "Falta la configuración local de Supabase en este dispositivo."
                )
                return@launch
            }

            val playerId = when (val result = accountRepository.loadCurrentPlayerId()) {
                is CurrentPlayerIdResult.Success -> result.playerId
                CurrentPlayerIdResult.NotLinked -> {
                    _uiState.value = SettingsUiState(
                        isLoading = false,
                        errorMessage = "Tu cuenta todavía no está vinculada a un jugador de la liga."
                    )
                    return@launch
                }
                is CurrentPlayerIdResult.Failure -> {
                    _uiState.value = SettingsUiState(isLoading = false, errorMessage = result.message)
                    return@launch
                }
            }

            val playerName = when (val result = leagueRepository.loadSnapshot()) {
                is FootballSnapshotResult.Success -> result.snapshot.players
                    .firstOrNull { it.id == playerId }
                    ?.name
                is FootballSnapshotResult.Failure -> {
                    _uiState.value = SettingsUiState(isLoading = false, errorMessage = result.message)
                    return@launch
                }
            }

            if (playerName == null) {
                _uiState.value = SettingsUiState(
                    isLoading = false,
                    errorMessage = "No se ha encontrado tu jugador en la liga."
                )
                return@launch
            }

            when (val metadata = accountRepository.loadMetadata(playerId)) {
                is PlayerProfileMetadataResult.Success -> _uiState.value = SettingsUiState(
                    isLoading = false,
                    playerId = playerId,
                    playerName = playerName,
                    avatarUrl = metadata.metadata.avatarUrl
                )
                PlayerProfileMetadataResult.NotFound -> _uiState.value = SettingsUiState(
                    isLoading = false,
                    playerId = playerId,
                    playerName = playerName,
                    avatarWarning = "Tu avatar estará disponible cuando se complete la vinculación de la cuenta."
                )
                is PlayerProfileMetadataResult.Failure -> _uiState.value = SettingsUiState(
                    isLoading = false,
                    playerId = playerId,
                    playerName = playerName,
                    avatarWarning = metadata.message
                )
            }
        }
    }

    fun uploadAvatar(avatar: AvatarUpload) {
        val state = _uiState.value
        val repository = profileRepository
        if (repository == null || state.playerId == null || state.isUploadingAvatar) return

        _uiState.value = state.copy(
            isUploadingAvatar = true,
            avatarFeedbackMessage = null,
            avatarErrorMessage = null
        )
        viewModelScope.launch {
            when (val result = repository.uploadOwnAvatar(avatar)) {
                AvatarUploadResult.Success -> refreshAvatarMetadata(repository, checkNotNull(state.playerId))
                is AvatarUploadResult.Failure -> _uiState.value = _uiState.value.copy(
                    isUploadingAvatar = false,
                    avatarErrorMessage = result.message
                )
            }
        }
    }

    fun reportAvatarPickerFailure(message: String) {
        _uiState.value = _uiState.value.copy(
            avatarFeedbackMessage = null,
            avatarErrorMessage = message
        )
    }

    private suspend fun refreshAvatarMetadata(
        repository: PlayerProfileRepository,
        playerId: String
    ) {
        when (val metadata = repository.loadMetadata(playerId)) {
            is PlayerProfileMetadataResult.Success -> _uiState.value = _uiState.value.copy(
                isUploadingAvatar = false,
                avatarUrl = metadata.metadata.avatarUrl,
                avatarWarning = null,
                avatarFeedbackMessage = "Foto de perfil actualizada.",
                avatarErrorMessage = null
            )
            PlayerProfileMetadataResult.NotFound -> avatarRefreshFailed()
            is PlayerProfileMetadataResult.Failure -> avatarRefreshFailed(metadata.message)
        }
    }

    private fun avatarRefreshFailed(message: String = "La foto se ha guardado, pero no se ha podido recargar todavía.") {
        _uiState.value = _uiState.value.copy(
            isUploadingAvatar = false,
            avatarFeedbackMessage = "Foto de perfil actualizada.",
            avatarErrorMessage = message
        )
    }
}
