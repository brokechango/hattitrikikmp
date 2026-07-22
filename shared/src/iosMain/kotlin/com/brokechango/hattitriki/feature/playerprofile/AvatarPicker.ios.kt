package com.brokechango.hattitriki.feature.playerprofile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.brokechango.hattitriki.core.data.AvatarUpload
import io.github.ismoy.imagepickerkmp.domain.config.CropConfig
import io.github.ismoy.imagepickerkmp.domain.config.GalleryConfig
import io.github.ismoy.imagepickerkmp.domain.extensions.loadBytes
import io.github.ismoy.imagepickerkmp.domain.models.MimeType
import io.github.ismoy.imagepickerkmp.features.imagepicker.config.ImagePickerKMPConfig
import io.github.ismoy.imagepickerkmp.features.imagepicker.model.ImagePickerResult
import io.github.ismoy.imagepickerkmp.features.imagepicker.ui.rememberImagePickerKMP

@Composable
actual fun rememberAvatarPicker(
    onAvatarSelected: (AvatarUpload) -> Unit,
    onPickerFailure: (String) -> Unit
): AvatarPicker {
    val latestAvatarSelected = rememberUpdatedState(onAvatarSelected)
    val latestPickerFailure = rememberUpdatedState(onPickerFailure)
    val nativePicker = rememberImagePickerKMP(
        config = ImagePickerKMPConfig(
            cropConfig = CropConfig(
                enabled = true,
                squareCrop = true,
                circularCrop = true,
                freeformCrop = false
            ),
            galleryConfig = GalleryConfig(
                allowMultiple = false,
                selectionLimit = 1,
                mimeTypes = avatarMimeTypes
            )
        )
    )
    val result = nativePicker.result

    LaunchedEffect(result) {
        when (result) {
            is ImagePickerResult.Success -> {
                val photo = result.photos.firstOrNull()
                val contentType = photo?.mimeType?.lowercase()
                if (photo == null || contentType !in avatarContentTypes) {
                    latestPickerFailure.value("Selecciona una foto JPEG o WebP.")
                } else {
                    latestAvatarSelected.value(AvatarUpload(photo.loadBytes(), checkNotNull(contentType)))
                }
            }
            is ImagePickerResult.Error -> latestPickerFailure.value(
                result.exception.message ?: "No se ha podido abrir el selector de fotos."
            )
            else -> Unit
        }
    }

    return remember(nativePicker) {
        object : AvatarPicker {
            override val isPreparing: Boolean
                get() = nativePicker.result is ImagePickerResult.Loading

            override fun choosePhoto() {
                nativePicker.launchGallery(
                    allowMultiple = false,
                    selectionLimit = 1,
                    mimeTypes = avatarMimeTypes
                )
            }
        }
    }
}

private val avatarMimeTypes = listOf(MimeType.IMAGE_JPEG, MimeType.IMAGE_WEBP)
private val avatarContentTypes = setOf("image/jpeg", "image/webp")
