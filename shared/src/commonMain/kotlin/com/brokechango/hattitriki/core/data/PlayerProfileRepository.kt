package com.brokechango.hattitriki.core.data

import com.brokechango.hattitriki.core.logging.logSupabaseFailure
import com.brokechango.hattitriki.core.logging.logSupabaseRequest
import com.brokechango.hattitriki.core.logging.logSupabaseSuccess
import com.brokechango.hattitriki.core.supabase.SupabaseErrorMessages
import com.brokechango.hattitriki.core.supabase.toSupabaseUserMessage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.minutes

data class PlayerProfileMetadata(
    val playerId: String,
    val avatarUrl: String?,
    val avatarVersion: Int,
    val isCurrentPlayer: Boolean,
    val hasLinkedAccount: Boolean
)

/** A privacy-safe, already-normalized image prepared by the platform picker. */
data class AvatarUpload(
    val bytes: ByteArray,
    val contentType: String
)

sealed interface PlayerProfileMetadataResult {
    data class Success(val metadata: PlayerProfileMetadata) : PlayerProfileMetadataResult
    data object NotFound : PlayerProfileMetadataResult
    data class Failure(val message: String) : PlayerProfileMetadataResult
}

sealed interface CurrentPlayerIdResult {
    data class Success(val playerId: String) : CurrentPlayerIdResult
    data object NotLinked : CurrentPlayerIdResult
    data class Failure(val message: String) : CurrentPlayerIdResult
}

sealed interface PlayerAvatarUrlsResult {
    data class Success(val avatarUrlsByPlayerId: Map<String, String>) : PlayerAvatarUrlsResult
    data class Failure(val message: String) : PlayerAvatarUrlsResult
}

sealed interface AvatarUploadResult {
    data object Success : AvatarUploadResult
    data class Failure(val message: String) : AvatarUploadResult
}

interface PlayerProfileRepository {
    suspend fun loadCurrentPlayerId(): CurrentPlayerIdResult

    suspend fun loadLeagueAvatarUrls(): PlayerAvatarUrlsResult

    suspend fun loadMetadata(playerId: String): PlayerProfileMetadataResult

    suspend fun uploadOwnAvatar(avatar: AvatarUpload): AvatarUploadResult
}

@Serializable
private data class PlayerProfileInput(@SerialName("p_player_id") val playerId: String)

@Serializable
private data class StoredPlayerProfileMetadata(
    @SerialName("player_id") val playerId: String,
    @SerialName("avatar_path") val avatarPath: String? = null,
    @SerialName("avatar_version") val avatarVersion: Int = 0,
    @SerialName("is_current_player") val isCurrentPlayer: Boolean = false,
    @SerialName("has_linked_account") val hasLinkedAccount: Boolean = false
)

@Serializable
private data class StoredCurrentPlayerId(
    @SerialName("player_id") val playerId: String
)

@Serializable
private data class StoredLeaguePlayerAvatar(
    @SerialName("player_id") val playerId: String,
    @SerialName("avatar_path") val avatarPath: String,
    @SerialName("avatar_version") val avatarVersion: Int
)

@Serializable
private data class AvatarPathInput(@SerialName("p_avatar_path") val avatarPath: String)

class SupabasePlayerProfileRepository(
    private val client: SupabaseClient
) : PlayerProfileRepository {
    override suspend fun loadCurrentPlayerId(): CurrentPlayerIdResult = try {
        logSupabaseRequest("Cargar jugador de la cuenta")
        val playerId = client.postgrest
            .rpc("get_current_league_player_id")
            .decodeList<StoredCurrentPlayerId>()
            .firstOrNull()
            ?.playerId
            ?: return CurrentPlayerIdResult.NotLinked

        logSupabaseSuccess("Cargar jugador de la cuenta")
        CurrentPlayerIdResult.Success(playerId)
    } catch (exception: Exception) {
        logSupabaseFailure("Cargar jugador de la cuenta", exception)
        CurrentPlayerIdResult.Failure(currentPlayerLoadErrorMessage(exception))
    }

    override suspend fun loadLeagueAvatarUrls(): PlayerAvatarUrlsResult = try {
        logSupabaseRequest("Cargar avatares de la liga")
        val avatars = client.postgrest
            .rpc("get_league_player_avatars")
            .decodeList<StoredLeaguePlayerAvatar>()
        if (avatars.isEmpty()) return PlayerAvatarUrlsResult.Success(emptyMap())

        val signedUrls = client.storage
            .from(AVATAR_BUCKET)
            .createSignedUrls(10.minutes, avatars.map(StoredLeaguePlayerAvatar::avatarPath))
        check(signedUrls.size == avatars.size) { "Supabase no ha firmado todos los avatares." }

        logSupabaseSuccess("Cargar avatares de la liga")
        PlayerAvatarUrlsResult.Success(
            avatars.zip(signedUrls).associate { (avatar, signedUrl) ->
                avatar.playerId to signedUrl.signedURL.withAvatarVersion(avatar.avatarVersion)
            }
        )
    } catch (exception: Exception) {
        logSupabaseFailure("Cargar avatares de la liga", exception)
        PlayerAvatarUrlsResult.Failure(playerProfileLoadErrorMessage(exception))
    }

    override suspend fun loadMetadata(playerId: String): PlayerProfileMetadataResult = try {
        logSupabaseRequest("Cargar perfil de jugador")
        val stored = client.postgrest
            .rpc("get_league_player_profile", PlayerProfileInput(playerId))
            .decodeList<StoredPlayerProfileMetadata>()
            .firstOrNull()
            ?: return PlayerProfileMetadataResult.NotFound
        val avatarUrl = signedAvatarUrl(stored.avatarPath, stored.avatarVersion)
        logSupabaseSuccess("Cargar perfil de jugador")
        PlayerProfileMetadataResult.Success(
            PlayerProfileMetadata(
                playerId = stored.playerId,
                avatarUrl = avatarUrl,
                avatarVersion = stored.avatarVersion,
                isCurrentPlayer = stored.isCurrentPlayer,
                hasLinkedAccount = stored.hasLinkedAccount
            )
        )
    } catch (exception: Exception) {
        logSupabaseFailure("Cargar perfil de jugador", exception)
        PlayerProfileMetadataResult.Failure(playerProfileLoadErrorMessage(exception))
    }

    override suspend fun uploadOwnAvatar(avatar: AvatarUpload): AvatarUploadResult {
        if (avatar.bytes.isEmpty()) return AvatarUploadResult.Failure("No se ha podido preparar la foto elegida.")
        if (avatar.bytes.size > MAX_AVATAR_BYTES) {
            return AvatarUploadResult.Failure("La foto optimizada supera el límite de 2,5 MB. Elige otra foto más sencilla.")
        }
        val contentType = when (avatar.contentType.lowercase()) {
            AVATAR_WEBP_CONTENT_TYPE -> ContentType("image", "webp")
            AVATAR_JPEG_CONTENT_TYPE -> ContentType.Image.JPEG
            else -> return AvatarUploadResult.Failure("Selecciona una foto JPEG o WebP.")
        }
        val extension = if (contentType == ContentType.Image.JPEG) "jpg" else "webp"
        val userId = client.auth.currentUserOrNull()?.id
            ?: return AvatarUploadResult.Failure("Tu sesión ha caducado. Vuelve a iniciar sesión para actualizar la foto.")
        val path = "$userId/$userId.$extension"

        return try {
            logSupabaseRequest("Actualizar foto de perfil")
            client.storage.from(AVATAR_BUCKET).upload(path, avatar.bytes) {
                this.contentType = contentType
                upsert = true
            }
            // The version returned by the RPC is not used here. Awaiting the RPC still turns
            // non-success HTTP responses into exceptions, without treating a successful scalar
            // response as a failed avatar upload.
            client.postgrest.rpc("set_own_avatar", AvatarPathInput(path))
            logSupabaseSuccess("Actualizar foto de perfil")
            AvatarUploadResult.Success
        } catch (exception: Exception) {
            logSupabaseFailure("Actualizar foto de perfil", exception)
            AvatarUploadResult.Failure(avatarUploadErrorMessage(exception))
        }
    }

    private suspend fun signedAvatarUrl(path: String?, version: Int): String? {
        if (path.isNullOrBlank()) return null
        return try {
            val signedUrl = client.storage.from(AVATAR_BUCKET).createSignedUrl(path, 10.minutes)
            signedUrl.withAvatarVersion(version)
        } catch (exception: Exception) {
            logSupabaseFailure("Firmar avatar de jugador", exception)
            null
        }
    }
}

private fun String.withAvatarVersion(version: Int): String =
    "$this${if ('?' in this) '&' else '?'}avatar_version=$version"

private const val AVATAR_BUCKET = "avatars"
private const val AVATAR_JPEG_CONTENT_TYPE = "image/jpeg"
private const val AVATAR_WEBP_CONTENT_TYPE = "image/webp"
private const val MAX_AVATAR_BYTES = 2_500_000

internal fun playerProfileLoadErrorMessage(error: Throwable): String =
    error.toSupabaseUserMessage(
        SupabaseErrorMessages(
            setupMessage = "Los perfiles de jugador todavía no están configurados. Aplica la última migración de Supabase.",
            permissionMessage = "No tienes permisos para consultar perfiles de la liga.",
            connectionMessage = "No se ha podido cargar el perfil. Comprueba tu conexión e inténtalo de nuevo.",
            fallbackMessage = "No se ha podido cargar el perfil del jugador. Inténtalo de nuevo."
        )
    )

internal fun currentPlayerLoadErrorMessage(error: Throwable): String =
    error.toSupabaseUserMessage(
        SupabaseErrorMessages(
            setupMessage = "Los perfiles de jugador todavía no están configurados. Aplica la última migración de Supabase.",
            permissionMessage = "No tienes permisos para consultar el perfil de tu cuenta.",
            connectionMessage = "No se ha podido cargar tu perfil. Comprueba tu conexión e inténtalo de nuevo.",
            fallbackMessage = "No se ha podido cargar el perfil de tu cuenta. Inténtalo de nuevo."
        )
    )

internal fun avatarUploadErrorMessage(error: Throwable): String =
    error.toSupabaseUserMessage(
        SupabaseErrorMessages(
            setupMessage = "La subida de fotos todavía no está configurada. Aplica la última migración de Supabase.",
            permissionMessage = "Sólo puedes cambiar la foto de tu propio perfil.",
            connectionMessage = "No se ha podido subir la foto. Comprueba tu conexión e inténtalo de nuevo.",
            fallbackMessage = "No se ha podido actualizar la foto. Prueba con otra imagen JPEG o WebP."
        )
    )
