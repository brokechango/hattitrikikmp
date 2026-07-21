package com.brokechango.hattitriki.feature.teamrandomizer

sealed interface TeamRandomizerEvent {
    data class TogglePlayer(val playerId: String) : TeamRandomizerEvent
    data class TeamCountChanged(val value: Int) : TeamRandomizerEvent
    data object Generate : TeamRandomizerEvent
    data object SelectAllPlayers : TeamRandomizerEvent
    data object ClearSelection : TeamRandomizerEvent
    data object ToggleStatsBalance : TeamRandomizerEvent
    data object ReloadRoster : TeamRandomizerEvent
    data object SaveDraft : TeamRandomizerEvent
    data object ClearDraft : TeamRandomizerEvent
}
