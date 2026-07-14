package com.brokechango.hattitriki.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

/**
 * Keeps an independent, saveable back stack for every bottom-navigation tab.
 *
 * [backStack] is the flattened stack consumed by NavDisplay. When a secondary
 * tab is active, Home remains beneath it so the system back action returns to
 * Home before the app is dismissed.
 */
@Composable
fun rememberHattitrikiNavigationState(): HattitrikiNavigationState {
    val homeBackStack = rememberNavBackStack(Screens.Home)
    val historyBackStack = rememberNavBackStack(Screens.History)
    val playersBackStack = rememberNavBackStack(Screens.Players)
    val adminBackStack = rememberNavBackStack(Screens.Admin)
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
    private val visibleBackStack = mutableStateListOf<NavKey>()

    init {
        rebuildVisibleBackStack()
    }

    val backStack: List<NavKey>
        get() = visibleBackStack

    val currentScreen: Screens
        get() = activeBackStack.last() as Screens

    val currentTopLevelScreen: Screens
        get() = activeTab.screen

    fun selectTopLevel(screen: Screens) {
        selectedTab.value = HattitrikiTab.from(screen).name
        rebuildVisibleBackStack()
    }

    fun navigate(screen: Screens) {
        val topLevelTab = HattitrikiTab.fromOrNull(screen)
        if (topLevelTab != null) {
            selectedTab.value = topLevelTab.name
        } else {
            activeBackStack.add(screen)
        }
        rebuildVisibleBackStack()
    }

    fun navigateBack() {
        if (activeBackStack.size > 1) {
            activeBackStack.removeAt(activeBackStack.lastIndex)
        } else if (activeTab != HattitrikiTab.Home) {
            selectedTab.value = HattitrikiTab.Home.name
        }
        rebuildVisibleBackStack()
    }

    private val activeTab: HattitrikiTab
        get() = HattitrikiTab.valueOf(selectedTab.value)

    private val activeBackStack: MutableList<NavKey>
        get() = checkNotNull(tabBackStacks[activeTab])

    private fun rebuildVisibleBackStack() {
        visibleBackStack.clear()
        visibleBackStack.addAll(checkNotNull(tabBackStacks[HattitrikiTab.Home]))
        if (activeTab != HattitrikiTab.Home) {
            visibleBackStack.addAll(activeBackStack)
        }
    }
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
