package com.brokechango.hattitriki.feature.teamrandomizer

sealed interface TeamRandomizerEvent {
    data class ParticipantsChanged(val value: String) : TeamRandomizerEvent
    data class TeamCountChanged(val value: String) : TeamRandomizerEvent
    data object Generate : TeamRandomizerEvent
    data object UseActiveRoster : TeamRandomizerEvent
    data object ToggleStatsBalance : TeamRandomizerEvent
    data object ReloadRoster : TeamRandomizerEvent
    data object LoadExample : TeamRandomizerEvent
    data object Clear : TeamRandomizerEvent
}
