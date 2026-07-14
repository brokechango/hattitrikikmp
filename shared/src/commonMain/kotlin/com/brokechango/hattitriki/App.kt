package com.brokechango.hattitriki

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.brokechango.hattitriki.core.design.CrestBlack
import com.brokechango.hattitriki.core.design.CrestGold
import com.brokechango.hattitriki.core.design.CrestNavy
import com.brokechango.hattitriki.core.design.CrestWhite
import com.brokechango.hattitriki.core.design.HattitrikiTheme
import com.brokechango.hattitriki.ui.composables.PitchBackground
import com.brokechango.hattitriki.core.navigation.Screens
import com.brokechango.hattitriki.core.navigation.rememberHattitrikiNavigationState
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
        val navigation = rememberHattitrikiNavigationState()
        val currentScreen = navigation.currentScreen

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
                    canNavigateBack = navigation.backStack.size > 1,
                    onBack = navigation::navigateBack
                )
            },
            bottomBar = {
                MainNavigationBar(
                    currentScreen = navigation.currentTopLevelScreen,
                    onNavigate = navigation::selectTopLevel
                )
            }
        ) { innerPadding ->
            PitchBackground(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(modifier = Modifier.widthIn(max = 1040.dp)) {
                        NavDisplay(
                            backStack = navigation.backStack,
                            onBack = navigation::navigateBack,
                            entryProvider = entryProvider {
                                entry<Screens.Home> {
                                    HomeScreen(
                                        viewModel = homeViewModel,
                                        onEvent = { event ->
                                            when (event) {
                                                HomeEvent.OpenAdmin -> navigation.navigate(Screens.Admin)
                                                HomeEvent.OpenHistory -> navigation.navigate(Screens.History)
                                                HomeEvent.OpenPlayers -> navigation.navigate(Screens.Players)
                                                is HomeEvent.OpenMatch -> navigation.navigate(
                                                    Screens.MatchDetail(event.matchId)
                                                )
                                            }
                                        }
                                    )
                                }

                                entry<Screens.History> {
                                    HistoryScreen(
                                        viewModel = historyViewModel,
                                        onEvent = { event ->
                                            when (event) {
                                                is HistoryEvent.OpenMatch -> navigation.navigate(
                                                    Screens.MatchDetail(event.matchId)
                                                )
                                            }
                                        }
                                    )
                                }

                                entry<Screens.Players> {
                                    PlayersScreen(
                                        viewModel = playersViewModel,
                                        onEvent = {}
                                    )
                                }

                                entry<Screens.Admin> {
                                    AdminScreen(viewModel = adminViewModel)
                                }

                                entry<Screens.MatchDetail> { screen ->
                                    val matchDetailViewModel = remember(screen.matchId) {
                                        MatchDetailViewModel(matchId = screen.matchId)
                                    }
                                    MatchDetailScreen(
                                        viewModel = matchDetailViewModel,
                                        onEvent = { event ->
                                            when (event) {
                                                MatchDetailEvent.Back -> navigation.navigateBack()
                                            }
                                        }
                                    )
                                }
                            },
                            transitionSpec = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(300)
                                ) togetherWith slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(300)
                                )
                            },
                            popTransitionSpec = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(300)
                                ) togetherWith slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(300)
                                )
                            },
                            predictivePopTransitionSpec = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(300)
                                ) togetherWith slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(300)
                                )
                            }

                        )
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
                text = if (currentScreen == Screens.Home) "HATTITRIKI FC" else topBarTitle(
                    currentScreen
                ),
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                maxLines = 1
            )
        },
        navigationIcon = {
            if (canNavigateBack) {
                androidx.compose.material3.TextButton(
                    onClick = onBack,
                    colors = ButtonDefaults.textButtonColors(contentColor = CrestGold)
                ) {
                    Text(
                        "‹",
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                    )
                }
            } else {
                Row(
                    modifier = Modifier.padding(start = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(Res.drawable.hattitriki_app_icon),
                        contentDescription = "Hattitriki FC",
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CrestNavy,
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
        containerColor = CrestNavy,
        contentColor = CrestWhite,
        tonalElevation = NavigationBarDefaults.Elevation
    ) {
        NavigationBarItem(
            selected = currentScreen == Screens.Home,
            onClick = { onNavigate(Screens.Home) },
            label = { Text("Inicio") },
            icon = { Text("⌂") },
            colors = navItemColors()
        )
        NavigationBarItem(
            selected = currentScreen == Screens.History,
            onClick = { onNavigate(Screens.History) },
            label = { Text("Partidos") },
            icon = { Text("≋") },
            colors = navItemColors()
        )
        NavigationBarItem(
            selected = currentScreen == Screens.Players,
            onClick = { onNavigate(Screens.Players) },
            label = { Text("Plantilla") },
            icon = { Text("◎") },
            colors = navItemColors()
        )
        NavigationBarItem(
            selected = currentScreen == Screens.Admin,
            onClick = { onNavigate(Screens.Admin) },
            label = { Text("Míster") },
            icon = { Text("⚙") },
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
