package com.brokechango.hattitriki.feature.playerprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brokechango.hattitriki.core.data.FootballSnapshotResult
import com.brokechango.hattitriki.core.data.FriendlyFootballRepository
import com.brokechango.hattitriki.core.data.AvatarUpload
import com.brokechango.hattitriki.core.data.AvatarUploadResult
import com.brokechango.hattitriki.core.data.PlayerProfileMetadata
import com.brokechango.hattitriki.core.data.PlayerProfileMetadataResult
import com.brokechango.hattitriki.core.data.PlayerProfileRepository
import com.brokechango.hattitriki.core.data.playerProfileSummary
import com.brokechango.hattitriki.core.model.PlayerProfileSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PlayerProfileUiState(
    val isLoading: Boolean = true,
    val isUploadingAvatar: Boolean = false,
    val summary: PlayerProfileSummary? = null,
    val metadata: PlayerProfileMetadata? = null,
    val avatarWarning: String? = null,
    val avatarFeedbackMessage: String? = null,
    val avatarErrorMessage: String? = null,
    val errorMessage: String? = null
)

class PlayerProfileViewModel(
    private val playerId: String,
    private val footballRepository: FriendlyFootballRepository?,
    private val profileRepository: PlayerProfileRepository?
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerProfileUiState())
    val uiState: StateFlow<PlayerProfileUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = PlayerProfileUiState(isLoading = true)
        viewModelScope.launch {
            val leagueRepository = footballRepository
            val metadataRepository = profileRepository
            if (leagueRepository == null || metadataRepository == null) {
                _uiState.value = PlayerProfileUiState(
                    isLoading = false,
                    errorMessage = "Falta la configuración local de Supabase en este dispositivo."
                )
                return@launch
            }

            when (val snapshotResult = withContext(Dispatchers.Default) { leagueRepository.loadSnapshot() }) {
                is FootballSnapshotResult.Failure -> _uiState.value = PlayerProfileUiState(
                    isLoading = false,
                    errorMessage = snapshotResult.message
                )
                is FootballSnapshotResult.Success -> {
                    val summary = withContext(Dispatchers.Default) {
                        snapshotResult.snapshot.playerProfileSummary(playerId)
                    }
                    if (summary == null) {
                        _uiState.value = PlayerProfileUiState(
                            isLoading = false,
                            errorMessage = "No se ha encontrado el jugador solicitado."
                        )
                        return@launch
                    }

                    when (val metadataResult = metadataRepository.loadMetadata(playerId)) {
                        is PlayerProfileMetadataResult.Success -> _uiState.value = PlayerProfileUiState(
                            isLoading = false,
                            summary = summary,
                            metadata = metadataResult.metadata
                        )
                        PlayerProfileMetadataResult.NotFound -> _uiState.value = PlayerProfileUiState(
                            isLoading = false,
                            summary = summary,
                            avatarWarning = "Este jugador todavía no tiene una cuenta vinculada."
                        )
                        is PlayerProfileMetadataResult.Failure -> _uiState.value = PlayerProfileUiState(
                            isLoading = false,
                            summary = summary,
                            avatarWarning = metadataResult.message
                        )
                    }
                }
            }
        }
    }

    fun uploadAvatar(avatar: AvatarUpload) {
        val state = _uiState.value
        val repository = profileRepository
        if (repository == null || state.metadata?.isCurrentPlayer != true || state.isUploadingAvatar) return

        _uiState.value = state.copy(
            isUploadingAvatar = true,
            avatarFeedbackMessage = null,
            avatarErrorMessage = null
        )
        viewModelScope.launch {
            when (val result = repository.uploadOwnAvatar(avatar)) {
                AvatarUploadResult.Success -> refreshAvatarMetadata(repository)
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

    private suspend fun refreshAvatarMetadata(repository: PlayerProfileRepository) {
        when (val metadataResult = repository.loadMetadata(playerId)) {
            is PlayerProfileMetadataResult.Success -> _uiState.value = _uiState.value.copy(
                isUploadingAvatar = false,
                metadata = metadataResult.metadata,
                avatarFeedbackMessage = "Foto de perfil actualizada.",
                avatarErrorMessage = null
            )
            PlayerProfileMetadataResult.NotFound -> _uiState.value = _uiState.value.copy(
                isUploadingAvatar = false,
                avatarFeedbackMessage = "Foto de perfil actualizada.",
                avatarErrorMessage = null
            )
            is PlayerProfileMetadataResult.Failure -> _uiState.value = _uiState.value.copy(
                isUploadingAvatar = false,
                avatarFeedbackMessage = "Foto de perfil actualizada.",
                avatarErrorMessage = "La foto se ha guardado, pero no se ha podido recargar todavía."
            )
        }
    }
}
