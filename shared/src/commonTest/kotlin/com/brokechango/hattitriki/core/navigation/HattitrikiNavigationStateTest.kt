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

    @Test
    fun `selecting a tab returns to its root instead of restoring its detail`() {
        val navigation = navigationState()

        navigation.selectTopLevel(Screens.History)
        navigation.navigate(Screens.MatchDetail("match-42"))
        navigation.selectTopLevel(Screens.Players)

        navigation.selectTopLevel(Screens.History)

        assertEquals(Screens.History, navigation.currentScreen)
        assertEquals(listOf(Screens.Home, Screens.History), navigation.backStackFor(Screens.History))
    }

    @Test
    fun `back from generated teams returns to the generator`() {
        val navigation = navigationState()
        val openTime = 42L
        val generator = Screens.TeamRandomizer(openTime)

        navigation.navigate(Screens.Admin)
        navigation.navigate(generator)
        navigation.navigate(Screens.TeamRandomizerResult(openTime))

        navigation.navigateBack()

        assertEquals(generator, navigation.currentScreen)
        assertEquals(
            listOf(Screens.Home, Screens.Admin, generator),
            navigation.backStackFor(Screens.Admin)
        )
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
