package com.brokechango.hattitriki.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.brokechango.hattitriki.core.data.AvatarUpload
import com.brokechango.hattitriki.core.design.CrestGold
import com.brokechango.hattitriki.core.design.CrestNavyLight
import com.brokechango.hattitriki.feature.playerprofile.AvatarUploadControl
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.ScreenTitle
import com.brokechango.hattitriki.ui.composables.SupabaseLoadingState
import com.brokechango.hattitriki.ui.preview.HattitrikiPreview
import com.brokechango.hattitriki.ui.preview.PreviewTargets
import com.github.panpf.sketch.AsyncImage

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    accountEmail: String,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenTitle(
            title = "Ajustes",
            subtitle = "Gestiona tu cuenta de la liga"
        )

        when {
            uiState.isLoading -> SupabaseLoadingState(message = "Cargando ajustes…")
            uiState.errorMessage != null -> SettingsError(
                message = checkNotNull(uiState.errorMessage),
                onRetry = viewModel::refresh
            )
            uiState.playerName != null -> SettingsContent(
                state = uiState,
                accountEmail = accountEmail,
                onAvatarSelected = viewModel::uploadAvatar,
                onAvatarPickerFailure = viewModel::reportAvatarPickerFailure
            )
        }
    }
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    accountEmail: String,
    onAvatarSelected: (AvatarUpload) -> Unit,
    onAvatarPickerFailure: (String) -> Unit
) {
    val playerName = checkNotNull(state.playerName)

    FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsAvatar(name = playerName, avatarUrl = state.avatarUrl)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = playerName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "TU CUENTA",
                    style = MaterialTheme.typography.labelMedium,
                    color = CrestGold,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = accountEmail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    AvatarUploadControl(
        isUploading = state.isUploadingAvatar,
        successMessage = state.avatarFeedbackMessage,
        errorMessage = state.avatarErrorMessage,
        onAvatarSelected = onAvatarSelected,
        onPickerFailure = onAvatarPickerFailure
    )

    state.avatarWarning?.let { warning ->
        FootballCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = warning,
                modifier = Modifier.padding(14.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsAvatar(name: String, avatarUrl: String?) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(CrestNavyLight),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl != null) {
            AsyncImage(
                uri = avatarUrl,
                contentDescription = "Foto de perfil de $name",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = name.initials(),
                style = MaterialTheme.typography.headlineSmall,
                color = CrestGold,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun SettingsError(message: String, onRetry: () -> Unit) {
    FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "No se han podido abrir los ajustes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(message, color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

private fun String.initials(): String =
    trim().split(Regex("\\s+")).filter(String::isNotBlank).take(2).joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }

@PreviewTargets
@Composable
private fun SettingsScreenPreview() {
    HattitrikiPreview {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ScreenTitle(
                title = "Ajustes",
                subtitle = "Gestiona tu cuenta de la liga"
            )
            SettingsContent(
                state = SettingsUiState(
                    isLoading = false,
                    playerName = "Arturo García"
                ),
                accountEmail = "arturo@hattitriki.com",
                onAvatarSelected = {},
                onAvatarPickerFailure = {}
            )
        }
    }
}
