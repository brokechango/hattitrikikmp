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
    data class MatchDetail(val matchId: String) : Screens
}

val screensSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Screens.Home::class, Screens.Home.serializer())
            subclass(Screens.History::class, Screens.History.serializer())
            subclass(Screens.Players::class, Screens.Players.serializer())
            subclass(Screens.Admin::class, Screens.Admin.serializer())
            subclass(Screens.MatchDetail::class, Screens.MatchDetail.serializer())
        }
    }
}
