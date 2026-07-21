package com.brokechango.hattitriki.core.data

import com.brokechango.hattitriki.core.auth.AuthRepository
import com.brokechango.hattitriki.core.logging.logSupabaseFailure
import com.brokechango.hattitriki.core.logging.logSupabaseRequest
import com.brokechango.hattitriki.core.logging.logSupabaseSuccess
import com.brokechango.hattitriki.core.supabase.SupabaseErrorMessages
import com.brokechango.hattitriki.core.supabase.toSupabaseUserMessage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface CreatePlayerResult {
    data object Success : CreatePlayerResult
    data object Unauthorized : CreatePlayerResult
    data class Failure(val message: String) : CreatePlayerResult
}

sealed interface AdminPlayersResult {
    data class Success(val players: List<AdminPlayer>) : AdminPlayersResult
    data object Unauthorized : AdminPlayersResult
    data class Failure(val message: String) : AdminPlayersResult
}

sealed interface EditPlayerResult {
    data object Success : EditPlayerResult
    data object Unauthorized : EditPlayerResult
    data class Failure(val message: String) : EditPlayerResult
}

@Serializable
private data class CreatePlayerRpcInput(
    @SerialName("p_name") val name: String,
    @SerialName("p_has_cardio") val hasCardio: Boolean
)

@Serializable
private data class UpdatePlayerRpcInput(
    @SerialName("p_player_id") val playerId: String,
    @SerialName("p_name") val name: String,
    @SerialName("p_has_cardio") val hasCardio: Boolean
)

@Serializable
private data class PlayerIdRpcInput(@SerialName("p_player_id") val playerId: String)

/** Writes players through the admin's current Supabase session. */
class AdminPlayerRepository internal constructor(
    private val client: SupabaseClient,
    private val authRepository: AuthRepository
) {
    suspend fun createPlayer(name: String, hasCardio: Boolean): CreatePlayerResult {
        if (!authRepository.hasActiveAdminSession()) {
            return CreatePlayerResult.Unauthorized
        }

        return try {
            logSupabaseRequest("Crear jugador")
            client.postgrest.rpc(
                "create_active_player",
                CreatePlayerRpcInput(name.trim(), hasCardio)
            )
            logSupabaseSuccess("Crear jugador")
            CreatePlayerResult.Success
        } catch (exception: Exception) {
            logSupabaseFailure("Crear jugador", exception)
            CreatePlayerResult.Failure(
                playerSaveErrorMessage(exception)
            )
        }
    }

    suspend fun loadPlayers(): AdminPlayersResult {
        if (!authRepository.hasActiveAdminSession()) return AdminPlayersResult.Unauthorized
        return try {
            logSupabaseRequest("Cargar jugadores de administración")
            val players =
                client.postgrest.rpc("get_admin_players")
                    .decodeList<AdminPlayer>()
            logSupabaseSuccess("Cargar jugadores de administración")
            AdminPlayersResult.Success(players)
        } catch (exception: Exception) {
            logSupabaseFailure("Cargar jugadores de administración", exception)
            AdminPlayersResult.Failure(adminPlayersLoadErrorMessage(exception))
        }
    }

    suspend fun updatePlayer(playerId: String, name: String, hasCardio: Boolean): EditPlayerResult {
        if (!authRepository.hasActiveAdminSession()) return EditPlayerResult.Unauthorized
        return try {
            logSupabaseRequest("Actualizar jugador")
            client.postgrest.rpc("update_active_player", UpdatePlayerRpcInput(playerId, name.trim(), hasCardio))
            logSupabaseSuccess("Actualizar jugador")
            EditPlayerResult.Success
        } catch (exception: Exception) {
            logSupabaseFailure("Actualizar jugador", exception)
            EditPlayerResult.Failure(playerSaveErrorMessage(exception))
        }
    }

    suspend fun deletePlayer(playerId: String): EditPlayerResult {
        if (!authRepository.hasActiveAdminSession()) return EditPlayerResult.Unauthorized
        return try {
            logSupabaseRequest("Borrar jugador")
            client.postgrest.rpc("delete_player", PlayerIdRpcInput(playerId))
            logSupabaseSuccess("Borrar jugador")
            EditPlayerResult.Success
        } catch (exception: Exception) {
            logSupabaseFailure("Borrar jugador", exception)
            val message = exception.message.orEmpty()
            EditPlayerResult.Failure(
                if ("Player has match history" in message) {
                    "No se puede borrar un jugador que ya tiene historial de partidos."
                } else {
                    playerDeleteErrorMessage(exception)
                }
            )
        }
    }

    suspend fun hasActiveAdminSession(): Boolean = authRepository.hasActiveAdminSession()
}

/** Keeps PostgREST and database implementation details out of the UI. */
internal fun playerSaveErrorMessage(error: Throwable): String =
    error.toSupabaseUserMessage(
        SupabaseErrorMessages(
        setupMessage = "La configuración de jugadores en Supabase no está lista. Aplica la última migración y vuelve a intentarlo.",
        permissionMessage = "No tienes permisos para añadir jugadores. Vuelve a iniciar sesión como administrador.",
        connectionMessage = "No se ha podido conectar con Supabase. Comprueba tu conexión e inténtalo de nuevo.",
        fallbackMessage = "No se ha podido guardar el jugador. Inténtalo de nuevo.",
        conflictMessage = "Ya existe un jugador con ese nombre."
    )
)

internal fun adminPlayersLoadErrorMessage(error: Throwable): String =
    error.toSupabaseUserMessage(
        SupabaseErrorMessages(
            setupMessage = "La configuración de jugadores en Supabase no está lista. Aplica la última migración y vuelve a intentarlo.",
            permissionMessage = "No tienes permisos para consultar jugadores. Vuelve a iniciar sesión como administrador.",
            connectionMessage = "No se ha podido conectar con Supabase. Comprueba tu conexión e inténtalo de nuevo.",
            fallbackMessage = "No se han podido cargar los jugadores. Inténtalo de nuevo."
        )
    )

internal fun playerDeleteErrorMessage(error: Throwable): String =
    error.toSupabaseUserMessage(
        SupabaseErrorMessages(
            setupMessage = "La configuración de jugadores en Supabase no está lista. Aplica la última migración y vuelve a intentarlo.",
            permissionMessage = "No tienes permisos para borrar jugadores. Vuelve a iniciar sesión como administrador.",
            connectionMessage = "No se ha podido conectar con Supabase. Comprueba tu conexión e inténtalo de nuevo.",
            fallbackMessage = "No se ha podido borrar el jugador. Inténtalo de nuevo."
        )
    )
