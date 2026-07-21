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

enum class ActaTeam { A, B }

@Serializable
data class AdminPlayer(
    val id: String,
    val name: String,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("has_cardio") val hasCardio: Boolean = false
)

@Serializable
data class ActaParticipant(
    @SerialName("player_id") val playerId: String,
    val team: String,
    @SerialName("was_goalkeeper") val wasGoalkeeper: Boolean
)

@Serializable
data class ActaGoal(
    @SerialName("player_id") val playerId: String,
    val team: String,
    val count: Int,
    @SerialName("goalkeeper_id") val goalkeeperId: String,
    @SerialName("is_own_goal") val isOwnGoal: Boolean = false
)

@Serializable
private data class FriendlyMatchActaRpcInput(
    @SerialName("p_match_date") val matchDate: String,
    @SerialName("p_team_a_score") val teamAScore: Int,
    @SerialName("p_team_b_score") val teamBScore: Int,
    @SerialName("p_team_a_penalty_score") val teamAPenaltyScore: Int?,
    @SerialName("p_team_b_penalty_score") val teamBPenaltyScore: Int?,
    @SerialName("p_players") val participants: List<ActaParticipant>,
    @SerialName("p_goals") val goals: List<ActaGoal>
)

data class MatchReportDraft(
    val matchDate: String,
    val teamAScore: Int,
    val teamBScore: Int,
    val teamAPenaltyScore: Int?,
    val teamBPenaltyScore: Int?,
    val participants: List<ActaParticipant>,
    val goals: List<ActaGoal>
)

@Serializable
data class AdminMatchSummary(
    val id: String,
    @SerialName("played_on") val playedOn: String,
    @SerialName("team_a_score") val teamAScore: Int,
    @SerialName("team_b_score") val teamBScore: Int,
    @SerialName("team_a_penalty_score") val teamAPenaltyScore: Int? = null,
    @SerialName("team_b_penalty_score") val teamBPenaltyScore: Int? = null
)

@Serializable
private data class StoredMatchReport(
    @SerialName("match_date") val matchDate: String,
    @SerialName("team_a_score") val teamAScore: Int,
    @SerialName("team_b_score") val teamBScore: Int,
    @SerialName("team_a_penalty_score") val teamAPenaltyScore: Int?,
    @SerialName("team_b_penalty_score") val teamBPenaltyScore: Int?,
    val participants: List<ActaParticipant>,
    val goals: List<ActaGoal>
)

@Serializable
private data class MatchIdRpcInput(@SerialName("p_match_id") val matchId: String)

@Serializable
private data class UpdateFriendlyMatchActaRpcInput(
    @SerialName("p_match_id") val matchId: String,
    @SerialName("p_match_date") val matchDate: String,
    @SerialName("p_team_a_score") val teamAScore: Int,
    @SerialName("p_team_b_score") val teamBScore: Int,
    @SerialName("p_team_a_penalty_score") val teamAPenaltyScore: Int?,
    @SerialName("p_team_b_penalty_score") val teamBPenaltyScore: Int?,
    @SerialName("p_players") val participants: List<ActaParticipant>,
    @SerialName("p_goals") val goals: List<ActaGoal>
)

sealed interface LoadPlayersResult {
    data class Success(val players: List<AdminPlayer>) : LoadPlayersResult
    data object Unauthorized : LoadPlayersResult
    data class Failure(val message: String) : LoadPlayersResult
}

sealed interface CreateMatchResult {
    data object Success : CreateMatchResult
    data object Unauthorized : CreateMatchResult
    data class Failure(val message: String) : CreateMatchResult
}

sealed interface AdminMatchesResult {
    data class Success(val matches: List<AdminMatchSummary>) : AdminMatchesResult
    data object Unauthorized : AdminMatchesResult
    data class Failure(val message: String) : AdminMatchesResult
}

sealed interface LoadMatchResult {
    data class Success(val report: MatchReportDraft) : LoadMatchResult
    data object NotFound : LoadMatchResult
    data object Unauthorized : LoadMatchResult
    data class Failure(val message: String) : LoadMatchResult
}

sealed interface EditMatchResult {
    data object Success : EditMatchResult
    data object Unauthorized : EditMatchResult
    data class Failure(val message: String) : EditMatchResult
}

/**
 * Reads active players and persists a complete match report through the same
 * authenticated Supabase client used by the administrator login.
 */
class AdminMatchRepository internal constructor(
    private val client: SupabaseClient,
    private val authRepository: AuthRepository
) {
    suspend fun loadActivePlayers(): LoadPlayersResult {
        if (!authRepository.hasActiveAdminSession()) return LoadPlayersResult.Unauthorized

        return try {
            logSupabaseRequest("Cargar plantilla activa")
            val players = client.postgrest
                .rpc("get_active_players")
                .decodeList<AdminPlayer>()
                .sortedBy { it.name.lowercase() }
            logSupabaseSuccess("Cargar plantilla activa")
            LoadPlayersResult.Success(players)
        } catch (exception: Exception) {
            logSupabaseFailure("Cargar plantilla activa", exception)
            LoadPlayersResult.Failure(
                activePlayersLoadErrorMessage(exception)
            )
        }
    }

    suspend fun createMatch(draft: MatchReportDraft): CreateMatchResult {
        if (!authRepository.hasActiveAdminSession()) return CreateMatchResult.Unauthorized
        return try {
            logSupabaseRequest("Guardar acta")
            client.postgrest.rpc(
                "create_friendly_match_acta",
                FriendlyMatchActaRpcInput(
                    matchDate = draft.matchDate,
                    teamAScore = draft.teamAScore,
                    teamBScore = draft.teamBScore,
                    teamAPenaltyScore = draft.teamAPenaltyScore,
                    teamBPenaltyScore = draft.teamBPenaltyScore,
                    participants = draft.participants,
                    goals = draft.goals
                )
            )
            logSupabaseSuccess("Guardar acta")
            CreateMatchResult.Success
        } catch (exception: Exception) {
            logSupabaseFailure("Guardar acta", exception)
            CreateMatchResult.Failure(
                matchSaveErrorMessage(exception)
            )
        }
    }

    suspend fun loadMatches(): AdminMatchesResult {
        if (!authRepository.hasActiveAdminSession()) return AdminMatchesResult.Unauthorized
        return try {
            logSupabaseRequest("Cargar partidos de administración")
            val matches =
                client.postgrest.rpc("get_admin_friendly_matches")
                    .decodeList<AdminMatchSummary>()
            logSupabaseSuccess("Cargar partidos de administración")
            AdminMatchesResult.Success(matches)
        } catch (exception: Exception) {
            logSupabaseFailure("Cargar partidos de administración", exception)
            AdminMatchesResult.Failure(adminMatchesLoadErrorMessage(exception))
        }
    }

    suspend fun loadMatch(matchId: String): LoadMatchResult {
        if (!authRepository.hasActiveAdminSession()) return LoadMatchResult.Unauthorized
        return try {
            logSupabaseRequest("Cargar acta")
            val report = client.postgrest.rpc("get_friendly_match_acta", MatchIdRpcInput(matchId))
                .decodeList<StoredMatchReport>()
                .singleOrNull()
                ?: return LoadMatchResult.NotFound
            logSupabaseSuccess("Cargar acta")
            LoadMatchResult.Success(
                MatchReportDraft(
                    matchDate = report.matchDate,
                    teamAScore = report.teamAScore,
                    teamBScore = report.teamBScore,
                    teamAPenaltyScore = report.teamAPenaltyScore,
                    teamBPenaltyScore = report.teamBPenaltyScore,
                    participants = report.participants,
                    goals = report.goals
                )
            )
        } catch (exception: Exception) {
            logSupabaseFailure("Cargar acta", exception)
            LoadMatchResult.Failure(matchLoadErrorMessage(exception))
        }
    }

    suspend fun updateMatch(matchId: String, draft: MatchReportDraft): EditMatchResult {
        if (!authRepository.hasActiveAdminSession()) return EditMatchResult.Unauthorized
        return try {
            logSupabaseRequest("Actualizar acta")
            client.postgrest.rpc(
                "update_friendly_match_acta",
                UpdateFriendlyMatchActaRpcInput(
                    matchId = matchId,
                    matchDate = draft.matchDate,
                    teamAScore = draft.teamAScore,
                    teamBScore = draft.teamBScore,
                    teamAPenaltyScore = draft.teamAPenaltyScore,
                    teamBPenaltyScore = draft.teamBPenaltyScore,
                    participants = draft.participants,
                    goals = draft.goals
                )
            )
            logSupabaseSuccess("Actualizar acta")
            EditMatchResult.Success
        } catch (exception: Exception) {
            logSupabaseFailure("Actualizar acta", exception)
            EditMatchResult.Failure(matchUpdateErrorMessage(exception))
        }
    }

    suspend fun deleteMatch(matchId: String): EditMatchResult {
        if (!authRepository.hasActiveAdminSession()) return EditMatchResult.Unauthorized
        return try {
            logSupabaseRequest("Borrar acta")
            client.postgrest.rpc("delete_friendly_match", MatchIdRpcInput(matchId))
            logSupabaseSuccess("Borrar acta")
            EditMatchResult.Success
        } catch (exception: Exception) {
            logSupabaseFailure("Borrar acta", exception)
            EditMatchResult.Failure(matchDeleteErrorMessage(exception))
        }
    }

    suspend fun hasActiveAdminSession(): Boolean = authRepository.hasActiveAdminSession()
}

internal fun activePlayersLoadErrorMessage(error: Throwable): String =
    error.toSupabaseUserMessage(
        SupabaseErrorMessages(
            setupMessage = "La configuración de jugadores en Supabase no está lista. Aplica la última migración y vuelve a intentarlo.",
            permissionMessage = "No tienes permisos para consultar jugadores. Vuelve a iniciar sesión como administrador.",
            connectionMessage = "No se ha podido conectar con Supabase. Comprueba tu conexión e inténtalo de nuevo.",
            fallbackMessage = "No se ha podido cargar la plantilla. Inténtalo de nuevo."
        )
    )

internal fun matchSaveErrorMessage(error: Throwable): String =
    error.toSupabaseUserMessage(
        SupabaseErrorMessages(
            setupMessage = "La configuración para guardar actas en Supabase no está lista. Aplica la última migración y vuelve a intentarlo.",
            permissionMessage = "No tienes permisos para guardar el acta. Vuelve a iniciar sesión como administrador.",
            connectionMessage = "No se ha podido conectar con Supabase. Comprueba tu conexión e inténtalo de nuevo.",
            fallbackMessage = "No se ha podido guardar el acta del partido. Inténtalo de nuevo."
        )
    )

internal fun adminMatchesLoadErrorMessage(error: Throwable): String =
    error.toSupabaseUserMessage(
        SupabaseErrorMessages(
            setupMessage = "La configuración de partidos en Supabase no está lista. Aplica la última migración y vuelve a intentarlo.",
            permissionMessage = "No tienes permisos para consultar partidos. Vuelve a iniciar sesión como administrador.",
            connectionMessage = "No se ha podido conectar con Supabase. Comprueba tu conexión e inténtalo de nuevo.",
            fallbackMessage = "No se han podido cargar los partidos. Inténtalo de nuevo."
        )
    )

internal fun matchLoadErrorMessage(error: Throwable): String =
    error.toSupabaseUserMessage(
        SupabaseErrorMessages(
            setupMessage = "La configuración de partidos en Supabase no está lista. Aplica la última migración y vuelve a intentarlo.",
            permissionMessage = "No tienes permisos para consultar el acta. Vuelve a iniciar sesión como administrador.",
            connectionMessage = "No se ha podido conectar con Supabase. Comprueba tu conexión e inténtalo de nuevo.",
            fallbackMessage = "No se ha podido cargar el acta. Inténtalo de nuevo."
        )
    )

internal fun matchUpdateErrorMessage(error: Throwable): String =
    error.toSupabaseUserMessage(
        SupabaseErrorMessages(
            setupMessage = "La configuración para guardar actas en Supabase no está lista. Aplica la última migración y vuelve a intentarlo.",
            permissionMessage = "No tienes permisos para actualizar el acta. Vuelve a iniciar sesión como administrador.",
            connectionMessage = "No se ha podido conectar con Supabase. Comprueba tu conexión e inténtalo de nuevo.",
            fallbackMessage = "No se ha podido actualizar el acta. Inténtalo de nuevo."
        )
    )

internal fun matchDeleteErrorMessage(error: Throwable): String =
    error.toSupabaseUserMessage(
        SupabaseErrorMessages(
            setupMessage = "La configuración de partidos en Supabase no está lista. Aplica la última migración y vuelve a intentarlo.",
            permissionMessage = "No tienes permisos para borrar partidos. Vuelve a iniciar sesión como administrador.",
            connectionMessage = "No se ha podido conectar con Supabase. Comprueba tu conexión e inténtalo de nuevo.",
            fallbackMessage = "No se ha podido borrar el partido. Inténtalo de nuevo."
        )
    )
