package com.brokechango.hattitriki

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.rememberNavBackStack
import com.brokechango.hattitriki.core.design.CrestBlack
import com.brokechango.hattitriki.core.design.CrestGold
import com.brokechango.hattitriki.core.design.CrestWhite
import com.brokechango.hattitriki.core.design.HattitrikiTheme
import com.brokechango.hattitriki.core.design.PitchBackground
import com.brokechango.hattitriki.core.navigation.Screens
import com.brokechango.hattitriki.core.navigation.screensSavedStateConfiguration
import com.brokechango.hattitriki.feature.admin.AdminScreen
import com.brokechango.hattitriki.feature.admin.AdminViewModel
import com.brokechango.hattitriki.feature.history.HistoryEvent
import com.brokechango.hattitriki.feature.history.HistoryScreen
import com.brokechango.hattitriki.feature.history.HistoryViewModel
import com.brokechango.hattitriki.feature.home.HomeEvent
import com.brokechango.hattitriki.feature.home.HomeScreen
import com.brokechango.hattitriki.feature.home.HomeViewModel
import com.brokechango.hattitriki.feature.matchdetail.MatchDetailEvent
import com.brokechango.hattitriki.feature.matchdetail.MatchDetailScreen
import com.brokechango.hattitriki.feature.matchdetail.MatchDetailViewModel
import com.brokechango.hattitriki.feature.players.PlayersScreen
import com.brokechango.hattitriki.feature.players.PlayersViewModel
import hattitriki.shared.generated.resources.Res
import hattitriki.shared.generated.resources.hattitriki_app_icon
import org.jetbrains.compose.resources.painterResource

@Composable
@Preview
@OptIn(ExperimentalMaterial3Api::class)
fun App() {
    HattitrikiTheme {
        val backStack = rememberNavBackStack(screensSavedStateConfiguration, Screens.Home)
        val currentScreen = backStack.last() as Screens

        val homeViewModel = remember { HomeViewModel() }
        val historyViewModel = remember { HistoryViewModel() }
        val playersViewModel = remember { PlayersViewModel() }
        val adminViewModel = remember { AdminViewModel() }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            topBar = {
                MatchTopBar(
                    currentScreen = currentScreen,
                    canNavigateBack = backStack.size > 1,
                    onBack = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    }
                )
            },
            bottomBar = {
                MainNavigationBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        if (backStack.last() != screen) {
                            backStack.add(screen)
                        }
                    }
                )
            }
        ) { innerPadding ->
            PitchBackground(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    when (val screen = currentScreen) {
                        Screens.Home -> HomeScreen(
                            viewModel = homeViewModel,
                            onEvent = { event ->
                                when (event) {
                                    HomeEvent.OpenAdmin -> backStack.add(Screens.Admin)
                                    HomeEvent.OpenHistory -> backStack.add(Screens.History)
                                    HomeEvent.OpenPlayers -> backStack.add(Screens.Players)
                                    is HomeEvent.OpenMatch -> backStack.add(Screens.MatchDetail(event.matchId))
                                }
                            }
                        )

                        Screens.History -> HistoryScreen(
                            viewModel = historyViewModel,
                            onEvent = { event ->
                                when (event) {
                                    is HistoryEvent.OpenMatch -> backStack.add(Screens.MatchDetail(event.matchId))
                                }
                            }
                        )

                        Screens.Players -> PlayersScreen(
                            viewModel = playersViewModel,
                            onEvent = {}
                        )

                        Screens.Admin -> AdminScreen(viewModel = adminViewModel)

                        is Screens.MatchDetail -> {
                            val matchDetailViewModel = remember(screen.matchId) {
                                MatchDetailViewModel(matchId = screen.matchId)
                            }
                            MatchDetailScreen(
                                viewModel = matchDetailViewModel,
                                onEvent = { event ->
                                    when (event) {
                                        MatchDetailEvent.Back -> {
                                            if (backStack.size > 1) {
                                                backStack.removeAt(backStack.lastIndex)
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchTopBar(
    currentScreen: Screens,
    canNavigateBack: Boolean,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = topBarTitle(currentScreen),
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        },
        navigationIcon = {
            if (canNavigateBack) {
                androidx.compose.material3.TextButton(
                    onClick = onBack,
                    colors = ButtonDefaults.textButtonColors(contentColor = CrestGold)
                ) {
                    Text("<")
                }
            } else {
                Image(
                    painter = painterResource(Res.drawable.hattitriki_app_icon),
                    contentDescription = "Hattitriki FC",
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(36.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CrestBlack,
            titleContentColor = CrestWhite,
            navigationIconContentColor = CrestGold,
            actionIconContentColor = CrestWhite
        )
    )
}

private fun topBarTitle(screen: Screens): String = when (screen) {
    Screens.Home -> "Hattitriki FC"
    Screens.History -> "Marcador historico"
    Screens.Players -> "Plantilla"
    Screens.Admin -> "Zona mister"
    is Screens.MatchDetail -> "Acta del partido"
}

@Composable
private fun MainNavigationBar(
    currentScreen: Screens,
    onNavigate: (Screens) -> Unit
) {
    NavigationBar(
        containerColor = CrestBlack,
        contentColor = CrestWhite,
        tonalElevation = NavigationBarDefaults.Elevation
    ) {
        NavigationBarItem(
            selected = currentScreen == Screens.Home,
            onClick = { onNavigate(Screens.Home) },
            label = { Text("Inicio") },
            icon = { Text("1") },
            colors = navItemColors()
        )
        NavigationBarItem(
            selected = currentScreen == Screens.History,
            onClick = { onNavigate(Screens.History) },
            label = { Text("Historial") },
            icon = { Text("M") },
            colors = navItemColors()
        )
        NavigationBarItem(
            selected = currentScreen == Screens.Players,
            onClick = { onNavigate(Screens.Players) },
            label = { Text("Stats") },
            icon = { Text("G") },
            colors = navItemColors()
        )
        NavigationBarItem(
            selected = currentScreen == Screens.Admin,
            onClick = { onNavigate(Screens.Admin) },
            label = { Text("Admin") },
            icon = { Text("C") },
            colors = navItemColors()
        )
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = CrestBlack,
    selectedTextColor = CrestGold,
    indicatorColor = CrestGold,
    unselectedIconColor = CrestWhite.copy(alpha = 0.72f),
    unselectedTextColor = CrestWhite.copy(alpha = 0.72f)
)
