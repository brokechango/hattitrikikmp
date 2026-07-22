package com.brokechango.hattitriki.core.data

import com.brokechango.hattitriki.core.auth.AuthRepository
import com.brokechango.hattitriki.core.logging.logSupabaseFailure
import com.brokechango.hattitriki.core.logging.logSupabaseRequest
import com.brokechango.hattitriki.core.logging.logSupabaseSuccess
import com.brokechango.hattitriki.core.supabase.SupabaseErrorMessages
import com.brokechango.hattitriki.core.supabase.toSupabaseUserMessage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface SendLeagueInvitationResult {
    data class Success(val email: String) : SendLeagueInvitationResult
    data object Unauthorized : SendLeagueInvitationResult
    data class Failure(val message: String) : SendLeagueInvitationResult
}

interface LeagueInvitationGateway {
    suspend fun hasActiveAdminSession(): Boolean

    suspend fun loadPlayers(): AdminPlayersResult

    suspend fun sendInvitation(playerId: String, email: String): SendLeagueInvitationResult
}

@Serializable
private data class SendLeagueInvitationPayload(
    @SerialName("playerId") val playerId: String,
    val email: String
)

/**
 * Sends invitations through an Edge Function. The application only sends the
 * administrator's session token; the service-role key remains server-side.
 */
class LeagueInvitationRepository internal constructor(
    private val client: SupabaseClient,
    private val authRepository: AuthRepository
) : LeagueInvitationGateway {
    override suspend fun hasActiveAdminSession(): Boolean = authRepository.hasActiveAdminSession()

    override suspend fun loadPlayers(): AdminPlayersResult {
        if (!authRepository.hasActiveAdminSession()) return AdminPlayersResult.Unauthorized

        return try {
            val players = client.postgrest.rpc("get_invitable_players").decodeList<AdminPlayer>()
            AdminPlayersResult.Success(players)
        } catch (exception: Exception) {
            AdminPlayersResult.Failure(adminPlayersLoadErrorMessage(exception))
        }
    }

    override suspend fun sendInvitation(playerId: String, email: String): SendLeagueInvitationResult {
        if (!authRepository.hasActiveAdminSession()) {
            return SendLeagueInvitationResult.Unauthorized
        }

        val normalizedEmail = email.trim().lowercase()
        return try {
            logSupabaseRequest("Enviar invitación a la liga")
            client.functions(
                function = "send-league-invitation",
                body = SendLeagueInvitationPayload(playerId, normalizedEmail)
            )
            logSupabaseSuccess("Enviar invitación a la liga")
            SendLeagueInvitationResult.Success(normalizedEmail)
        } catch (exception: Exception) {
            logSupabaseFailure("Enviar invitación a la liga", exception)
            SendLeagueInvitationResult.Failure(invitationSendErrorMessage(exception))
        }
    }
}

internal fun invitationSendErrorMessage(error: Throwable): String {
    val details = error.message.orEmpty()
    return when {
        "invalid_email" in details -> "Indica un correo electrónico válido."
        "player_already_linked" in details || "player already linked" in details ||
            "player already has" in details ->
            "Ese jugador ya tiene una cuenta vinculada o una invitación pendiente."
        "email_already_registered" in details || "email_already_invited" in details ->
            "Ese correo ya tiene una cuenta o una invitación pendiente."
        else -> error.toSupabaseUserMessage(
            SupabaseErrorMessages(
                setupMessage = "El envío de invitaciones todavía no está configurado. Despliega la función de Supabase y vuelve a intentarlo.",
                permissionMessage = "Tu sesión ya no tiene permisos para enviar invitaciones.",
                connectionMessage = "No se ha podido enviar la invitación. Comprueba tu conexión e inténtalo de nuevo.",
                fallbackMessage = "No se ha podido enviar la invitación. Inténtalo de nuevo."
            )
        )
    }
}
