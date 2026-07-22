package com.brokechango.hattitriki.feature.invitation

sealed interface LeagueInvitationEvent {
    data class EmailChanged(val value: String) : LeagueInvitationEvent
    data class PlayerSelected(val playerId: String) : LeagueInvitationEvent
    data object RetryPlayers : LeagueInvitationEvent
    data object Submit : LeagueInvitationEvent
    data object SendAnother : LeagueInvitationEvent
}
