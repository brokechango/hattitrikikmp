package com.brokechango.hattitriki.core.data

import com.brokechango.hattitriki.core.logging.logSupabaseFailure
import com.brokechango.hattitriki.core.logging.logSupabaseRequest
import com.brokechango.hattitriki.core.logging.logSupabaseSuccess
import com.brokechango.hattitriki.core.model.FriendlyMatch
import com.brokechango.hattitriki.core.model.GoalEntry
import com.brokechango.hattitriki.core.model.MatchPlayer
import com.brokechango.hattitriki.core.model.PenaltyShootout
import com.brokechango.hattitriki.core.model.Player
import com.brokechango.hattitriki.core.model.PlayerStats
import com.brokechango.hattitriki.core.model.TeamSide
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class FootballSnapshot(
    val players: List<Player>,
    val matches: List<FriendlyMatch>
)

sealed interface FootballSnapshotResult {
    data class Success(val snapshot: FootballSnapshot) : FootballSnapshotResult
    data class Failure(val message: String) : FootballSnapshotResult
}

interface FriendlyFootballRepository {
    suspend fun loadSnapshot(): FootballSnapshotResult
}

/**
 * Public, read-only source for the league. The database RPCs deliberately
 * expose only the fields needed by the public screens; write access remains
 * limited to the existing administrator RPCs and RLS policies.
 */
class SupabaseFriendlyFootballRepository(
    private val client: SupabaseClient
) : FriendlyFootballRepository {
    override suspend fun loadSnapshot(): FootballSnapshotResult = try {
        logSupabaseRequest("Cargar datos públicos de la liga")
        val players = client.postgrest
            .rpc("get_public_league_players")
            .decodeList<StoredPublicPlayer>()
            .map { Player(id = it.id, name = it.name, isActive = it.isActive) }
        val matches = client.postgrest
            .rpc("get_public_friendly_matches")
            .decodeList<StoredPublicMatch>()
            .map(StoredPublicMatch::toFriendlyMatch)

        logSupabaseSuccess("Cargar datos públicos de la liga")
        FootballSnapshotResult.Success(
            FootballSnapshot(
                players = players,
                matches = matches
            )
        )
    } catch (exception: Exception) {
        logSupabaseFailure("Cargar datos públicos de la liga", exception)
        FootballSnapshotResult.Failure(publicLeagueLoadErrorMessage(exception.message))
    }
}

@Serializable
private data class StoredPublicPlayer(
    val id: String,
    val name: String,
    @SerialName("is_active") val isActive: Boolean
)

@Serializable
private data class StoredPublicMatch(
    val id: String,
    @SerialName("played_on") val playedOn: String,
    @SerialName("team_a_score") val teamAScore: Int,
    @SerialName("team_b_score") val teamBScore: Int,
    @SerialName("team_a_penalty_score") val teamAPenaltyScore: Int? = null,
    @SerialName("team_b_penalty_score") val teamBPenaltyScore: Int? = null,
    val participants: List<StoredPublicParticipant>,
    val goals: List<StoredPublicGoal>
) {
    fun toFriendlyMatch(): FriendlyMatch = FriendlyMatch(
        id = id,
        dateLabel = playedOn.toDisplayDate(),
        teamAScore = teamAScore,
        teamBScore = teamBScore,
        players = participants.map {
            MatchPlayer(
                playerId = it.playerId,
                team = it.team.toTeamSide(),
                wasGoalkeeper = it.wasGoalkeeper
            )
        },
        goals = goals.map {
            GoalEntry(
                playerId = it.playerId,
                team = it.team.toTeamSide(),
                count = it.count,
                goalkeeperId = it.goalkeeperId,
                isOwnGoal = it.isOwnGoal
            )
        },
        penaltyShootout = if (teamAPenaltyScore != null && teamBPenaltyScore != null) {
            PenaltyShootout(teamAPenaltyScore, teamBPenaltyScore)
        } else {
            null
        }
    )
}

private fun String.toDisplayDate(): String {
    val dateParts = split('-')
    return if (dateParts.size == 3 && dateParts.all { it.isNotBlank() }) {
        "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}"
    } else {
        this
    }
}

@Serializable
private data class StoredPublicParticipant(
    @SerialName("player_id") val playerId: String,
    val team: String,
    @SerialName("was_goalkeeper") val wasGoalkeeper: Boolean
)

@Serializable
private data class StoredPublicGoal(
    @SerialName("player_id") val playerId: String,
    val team: String,
    val count: Int,
    @SerialName("goalkeeper_id") val goalkeeperId: String,
    @SerialName("is_own_goal") val isOwnGoal: Boolean = false
)

private fun String.toTeamSide(): TeamSide = when (this) {
    "A" -> TeamSide.A
    "B" -> TeamSide.B
    else -> error("Unknown team side: $this")
}

fun FootballSnapshot.playerStats(): List<PlayerStats> = players.map { player ->
    val playedMatches = matches.filter { match ->
        match.players.any { it.playerId == player.id }
    }
    val wins = playedMatches.count { match ->
        val side = match.players.first { it.playerId == player.id }.team
        match.winner == side
    }
    val draws = playedMatches.count { it.winner == null }
    val goals = matches.sumOf { match ->
        match.goals
            .filter { it.playerId == player.id && !it.isOwnGoal }
            .sumOf { it.count }
    }
    val goalkeeperMatches = playedMatches.count { match ->
        match.players.any { it.playerId == player.id && it.wasGoalkeeper }
    }

    PlayerStats(
        player = player,
        matchesPlayed = playedMatches.size,
        wins = wins,
        draws = draws,
        losses = playedMatches.size - wins - draws,
        goals = goals,
        goalkeeperMatches = goalkeeperMatches
    )
}.sortedWith(compareByDescending<PlayerStats> { it.goals }.thenByDescending { it.wins })

internal fun publicLeagueLoadErrorMessage(errorDetails: String?): String = supabaseMessage(
    errorDetails = errorDetails,
    setupMessage = "La lectura pública de la liga no está configurada. Aplica la última migración de Supabase.",
    permissionMessage = "No tienes permisos para consultar los datos de la liga.",
    connectionMessage = "No se ha podido conectar con Supabase. Comprueba tu conexión e inténtalo de nuevo.",
    fallbackMessage = "No se han podido cargar los datos de la liga. Inténtalo de nuevo."
)
