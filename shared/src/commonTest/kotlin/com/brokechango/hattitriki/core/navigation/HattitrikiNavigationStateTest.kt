package com.brokechango.hattitriki.core.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HattitrikiNavigationStateTest {
    @Test
    fun `back from players returns to home instead of leaving the app`() {
        val navigation = navigationState()

        navigation.navigate(Screens.Players)

        assertTrue(navigation.canNavigateBack)
        assertEquals(listOf(Screens.Home, Screens.Players), navigation.backStackFor(Screens.Players))

        navigation.navigateBack()

        assertEquals(Screens.Home, navigation.currentTopLevelScreen)
        assertFalse(navigation.canNavigateBack)
    }

    @Test
    fun `back from a nested destination keeps its top-level tab before returning home`() {
        val navigation = navigationState()

        navigation.navigate(Screens.History)
        navigation.navigate(Screens.MatchDetail("match-42"))

        assertEquals(
            listOf(Screens.Home, Screens.History, Screens.MatchDetail("match-42")),
            navigation.backStackFor(Screens.History)
        )

        navigation.navigateBack()

        assertEquals(Screens.History, navigation.currentScreen)
        assertEquals(listOf(Screens.Home, Screens.History), navigation.backStackFor(Screens.History))
    }

    private fun navigationState(): HattitrikiNavigationState = HattitrikiNavigationState(
        selectedTab = mutableStateOf(HattitrikiTab.Home.name),
        tabBackStacks = mapOf(
            HattitrikiTab.Home to mutableListOf<NavKey>(Screens.Home),
            HattitrikiTab.History to mutableListOf<NavKey>(Screens.History),
            HattitrikiTab.Players to mutableListOf<NavKey>(Screens.Players),
            HattitrikiTab.Admin to mutableListOf<NavKey>(Screens.Admin)
        )
    )
}
