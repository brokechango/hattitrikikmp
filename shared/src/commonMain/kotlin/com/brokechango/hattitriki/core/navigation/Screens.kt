package com.brokechango.hattitriki.core.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
sealed interface Screens : NavKey {

    @Serializable
    data object Home : Screens

    @Serializable
    data object History : Screens

    @Serializable
    data object Players : Screens

    @Serializable
    data object Admin : Screens

    @Serializable
    data class NewMatch(val openTime: Long) : Screens

    @Serializable
    data object NewPlayer : Screens

    @Serializable
    data object ManageMatches : Screens

    @Serializable
    data object ManagePlayers : Screens

    @Serializable
    data class TeamRandomizer(val openTime: Long) : Screens

    @Serializable
    data class TeamRandomizerResult(val openTime: Long) : Screens

    @Serializable
    data class EditMatch(val matchId: String) : Screens

    @Serializable
    data class EditPlayer(val playerId: String) : Screens

    @Serializable
    data class MatchDetail(val matchId: String) : Screens
}

val screensSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Screens.Home::class, Screens.Home.serializer())
            subclass(Screens.History::class, Screens.History.serializer())
            subclass(Screens.Players::class, Screens.Players.serializer())
            subclass(Screens.Admin::class, Screens.Admin.serializer())
            subclass(Screens.NewMatch::class, Screens.NewMatch.serializer())
            subclass(Screens.NewPlayer::class, Screens.NewPlayer.serializer())
            subclass(Screens.ManageMatches::class, Screens.ManageMatches.serializer())
            subclass(Screens.ManagePlayers::class, Screens.ManagePlayers.serializer())
            subclass(Screens.TeamRandomizer::class, Screens.TeamRandomizer.serializer())
            subclass(
                Screens.TeamRandomizerResult::class,
                Screens.TeamRandomizerResult.serializer()
            )
            subclass(Screens.EditMatch::class, Screens.EditMatch.serializer())
            subclass(Screens.EditPlayer::class, Screens.EditPlayer.serializer())
            subclass(Screens.MatchDetail::class, Screens.MatchDetail.serializer())
        }
    }
}
