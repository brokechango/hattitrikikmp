package com.brokechango.hattitriki.feature.playerprofile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.brokechango.hattitriki.core.data.AvatarUpload
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class, ExperimentalEncodingApi::class)
@Composable
actual fun rememberAvatarPicker(
    onAvatarSelected: (AvatarUpload) -> Unit,
    onPickerFailure: (String) -> Unit
): AvatarPicker {
    val latestAvatarSelected = rememberUpdatedState(onAvatarSelected)
    val latestPickerFailure = rememberUpdatedState(onPickerFailure)
    var isPreparing by remember { mutableStateOf(false) }

    return remember {
        object : AvatarPicker {
            override val isPreparing: Boolean
                get() = isPreparing

            override fun choosePhoto() {
                if (isPreparing) return
                isPreparing = true
                launchWebAvatarPicker(
                    onSuccess = { encodedAvatar ->
                        isPreparing = false
                        runCatching { Base64.Default.decode(encodedAvatar) }
                            .onSuccess { bytes ->
                                latestAvatarSelected.value(AvatarUpload(bytes, "image/jpeg"))
                            }
                            .onFailure {
                                latestPickerFailure.value("No se ha podido preparar la foto elegida.")
                            }
                    },
                    onFailure = { message ->
                        isPreparing = false
                        latestPickerFailure.value(message)
                    },
                    onCancelled = { isPreparing = false }
                )
            }
        }
    }
}

/**
 * Browser implementation: it accepts the two server-approved types and emits a
 * centered square JPEG capped under the Storage bucket's 2.5 MB policy.
 */
@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """(onSuccess, onFailure, onCancelled) => {
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = 'image/jpeg,image/webp';
        input.onchange = async () => {
            const file = input.files && input.files[0];
            if (!file) { onCancelled(); return; }
            if (file.type !== 'image/jpeg' && file.type !== 'image/webp') {
                onFailure('Selecciona una foto JPEG o WebP.');
                return;
            }
            try {
                const sourceUrl = URL.createObjectURL(file);
                const image = new Image();
                image.onload = async () => {
                    try {
                        const crop = Math.min(image.naturalWidth, image.naturalHeight);
                        if (!crop) throw new Error('No se ha podido leer la imagen.');
                        const startX = (image.naturalWidth - crop) / 2;
                        const startY = (image.naturalHeight - crop) / 2;
                        const settings = [[640, .78], [512, .72], [400, .66], [320, .58]];
                        let blob = null;
                        for (const [dimension, quality] of settings) {
                            const canvas = document.createElement('canvas');
                            canvas.width = dimension;
                            canvas.height = dimension;
                            const context = canvas.getContext('2d');
                            if (!context) throw new Error('Tu navegador no permite optimizar la foto.');
                            context.drawImage(image, startX, startY, crop, crop, 0, 0, dimension, dimension);
                            blob = await new Promise(resolve => canvas.toBlob(resolve, 'image/jpeg', quality));
                            if (blob && blob.size <= 2500000) break;
                        }
                        URL.revokeObjectURL(sourceUrl);
                        if (!blob || blob.size > 2500000) {
                            throw new Error('No se ha podido optimizar la foto por debajo de 2,5 MB.');
                        }
                        const data = new Uint8Array(await blob.arrayBuffer());
                        let binary = '';
                        for (let offset = 0; offset < data.length; offset += 8192) {
                            binary += String.fromCharCode(...data.subarray(offset, offset + 8192));
                        }
                        onSuccess(btoa(binary));
                    } catch (error) {
                        URL.revokeObjectURL(sourceUrl);
                        onFailure(error instanceof Error ? error.message : 'No se ha podido preparar la foto.');
                    }
                };
                image.onerror = () => {
                    URL.revokeObjectURL(sourceUrl);
                    onFailure('No se ha podido leer la foto elegida.');
                };
                image.src = sourceUrl;
            } catch (error) {
                onFailure(error instanceof Error ? error.message : 'No se ha podido abrir el selector de fotos.');
            }
        };
        input.click();
    }"""
)
private external fun launchWebAvatarPicker(
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit,
    onCancelled: () -> Unit
)
