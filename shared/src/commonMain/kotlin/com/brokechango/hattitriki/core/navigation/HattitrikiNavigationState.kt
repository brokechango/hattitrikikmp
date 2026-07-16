package com.brokechango.hattitriki.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

/**
 * Keeps an independent, saveable back stack for every bottom-navigation tab.
 *
 * Every tab keeps its own stack. The app shell decides how tabs animate; the
 * stack supplied to NavDisplay is reserved for navigation inside that tab.
 */
@Composable
fun rememberHattitrikiNavigationState(): HattitrikiNavigationState {
    val homeBackStack = rememberNavBackStack(screensSavedStateConfiguration, Screens.Home)
    val historyBackStack = rememberNavBackStack(screensSavedStateConfiguration, Screens.History)
    val playersBackStack = rememberNavBackStack(screensSavedStateConfiguration, Screens.Players)
    val adminBackStack = rememberNavBackStack(screensSavedStateConfiguration, Screens.Admin)
    val selectedTab = rememberSaveable { mutableStateOf(HattitrikiTab.Home.name) }

    return remember(homeBackStack, historyBackStack, playersBackStack, adminBackStack) {
        HattitrikiNavigationState(
            selectedTab = selectedTab,
            tabBackStacks = mapOf(
                HattitrikiTab.Home to homeBackStack,
                HattitrikiTab.History to historyBackStack,
                HattitrikiTab.Players to playersBackStack,
                HattitrikiTab.Admin to adminBackStack
            )
        )
    }
}

class HattitrikiNavigationState internal constructor(
    private val selectedTab: MutableState<String>,
    private val tabBackStacks: Map<HattitrikiTab, MutableList<NavKey>>
) {
    val backStack: List<NavKey>
        get() = activeBackStack

    val currentScreen: Screens
        get() = activeBackStack.last() as Screens

    val currentTopLevelScreen: Screens
        get() = activeTab.screen

    val canNavigateBack: Boolean
        get() = activeBackStack.size > 1 || activeTab != HattitrikiTab.Home

    fun selectTopLevel(screen: Screens) {
        selectedTab.value = HattitrikiTab.from(screen).name
    }

    fun navigate(screen: Screens) {
        val topLevelTab = HattitrikiTab.fromOrNull(screen)
        if (topLevelTab != null) {
            selectedTab.value = topLevelTab.name
        } else {
            activeBackStack.add(screen)
        }
    }

    fun navigateBack() {
        if (activeBackStack.size > 1) {
            activeBackStack.removeAt(activeBackStack.lastIndex)
        } else if (activeTab != HattitrikiTab.Home) {
            selectedTab.value = HattitrikiTab.Home.name
        }
    }

    fun backStackFor(topLevelScreen: Screens): List<NavKey> {
        val tab = HattitrikiTab.from(topLevelScreen)
        val tabBackStack = checkNotNull(tabBackStacks[tab])

        // NavDisplay only registers the system back handler when it has a previous entry.
        // Keep Home below every top-level tab so pressing back from a tab returns there.
        return if (tab == HattitrikiTab.Home) {
            tabBackStack
        } else {
            checkNotNull(tabBackStacks[HattitrikiTab.Home]) + tabBackStack
        }
    }

    private val activeTab: HattitrikiTab
        get() = HattitrikiTab.valueOf(selectedTab.value)

    private val activeBackStack: MutableList<NavKey>
        get() = checkNotNull(tabBackStacks[activeTab])

}

internal enum class HattitrikiTab(val screen: Screens) {
    Home(Screens.Home),
    History(Screens.History),
    Players(Screens.Players),
    Admin(Screens.Admin);

    companion object {
        fun from(screen: Screens): HattitrikiTab = checkNotNull(fromOrNull(screen)) {
            "Match detail is not a top-level destination."
        }

        fun fromOrNull(screen: Screens): HattitrikiTab? = entries.firstOrNull { it.screen == screen }
    }
}
