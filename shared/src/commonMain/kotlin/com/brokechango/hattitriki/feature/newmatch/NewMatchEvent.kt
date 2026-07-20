package com.brokechango.hattitriki.feature.newmatch

import com.brokechango.hattitriki.core.data.ActaTeam

sealed interface NewMatchEvent {
    data class DateChanged(val value: String) : NewMatchEvent
    data class TeamAScoreChanged(val value: String) : NewMatchEvent
    data class TeamBScoreChanged(val value: String) : NewMatchEvent
    data object PenaltyShootoutToggled : NewMatchEvent
    data class TeamAPenaltyScoreChanged(val value: String) : NewMatchEvent
    data class TeamBPenaltyScoreChanged(val value: String) : NewMatchEvent
    data class TeamToggled(val playerId: String, val team: ActaTeam) : NewMatchEvent
    data class PlayerSetOutside(val playerId: String) : NewMatchEvent
    data class GoalkeeperToggled(val team: ActaTeam, val playerId: String) : NewMatchEvent
    data class GoalAdded(val goal: GoalDraft) : NewMatchEvent
    data class GoalRemoved(val goal: GoalDraft) : NewMatchEvent
    data object DiscardTeamsDraft : NewMatchEvent
    data object Submit : NewMatchEvent
}
