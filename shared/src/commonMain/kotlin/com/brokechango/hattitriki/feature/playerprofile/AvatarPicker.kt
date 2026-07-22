package com.brokechango.hattitriki.feature.playerprofile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.brokechango.hattitriki.core.data.AvatarUpload

/**
 * Gives the profile a platform-native image picker while keeping the upload
 * policy and the screen interaction identical on Android, iOS and the web.
 */
@Stable
interface AvatarPicker {
    val isPreparing: Boolean

    fun choosePhoto()
}

@Composable
expect fun rememberAvatarPicker(
    onAvatarSelected: (AvatarUpload) -> Unit,
    onPickerFailure: (String) -> Unit
): AvatarPicker
