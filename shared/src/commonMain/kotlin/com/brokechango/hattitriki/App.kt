package com.brokechango.hattitriki

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.runtime.LaunchedEffect
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
import com.brokechango.hattitriki.core.auth.AdminAuthRepository
import com.brokechango.hattitriki.core.data.AdminMatchRepository
import com.brokechango.hattitriki.core.data.AdminPlayerRepository
import com.brokechango.hattitriki.core.data.SupabaseFriendlyFootballRepository
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
import com.brokechango.hattitriki.feature.managematches.ManageMatchesScreen
import com.brokechango.hattitriki.feature.managematches.ManageMatchesViewModel
import com.brokechango.hattitriki.feature.manageplayers.ManagePlayersScreen
import com.brokechango.hattitriki.feature.manageplayers.ManagePlayersViewModel
import com.brokechango.hattitriki.feature.newmatch.NewMatchScreen
import com.brokechango.hattitriki.feature.newmatch.NewMatchViewModel
import com.brokechango.hattitriki.feature.newplayer.NewPlayerScreen
import com.brokechango.hattitriki.feature.newplayer.NewPlayerViewModel
import com.brokechango.hattitriki.feature.players.PlayersScreen
import com.brokechango.hattitriki.feature.players.PlayersEvent
import com.brokechango.hattitriki.feature.players.PlayersViewModel
import hattitriki.shared.generated.resources.Res
import hattitriki.shared.generated.resources.hattitriki_app_icon
import org.jetbrains.compose.resources.painterResource

@Composable
@Preview
@OptIn(ExperimentalMaterial3Api::class)
fun App(
    adminAuthRepository: AdminAuthRepository? = null,
    appVersion: String = "1.0"
) {
    HattitrikiTheme {
        val navigation = rememberHattitrikiNavigationState()
        val currentScreen = navigation.currentScreen
        val isPlayersScreen = currentScreen == Screens.Players
        val scrollState = rememberScrollState()

        val footballRepository = remember(adminAuthRepository) {
            adminAuthRepository?.let { SupabaseFriendlyFootballRepository(it.client) }
        }
        val homeViewModel = remember(footballRepository) { HomeViewModel(footballRepository) }
        val historyViewModel = remember(footballRepository) { HistoryViewModel(footballRepository) }
        val playersViewModel = remember(footballRepository) { PlayersViewModel(footballRepository) }
        val adminViewModel = remember(adminAuthRepository) { AdminViewModel(adminAuthRepository) }

        LaunchedEffect(currentScreen) {
            if (!isPlayersScreen) {
                scrollState.scrollTo(0)
            }
        }

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
                    modifier = (
                        Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                    ).let { modifier ->
                        if (isPlayersScreen) modifier else modifier.verticalScroll(scrollState)
                    }
                ) {
                    Box(
                        modifier = if (isPlayersScreen) {
                            Modifier.fillMaxSize().padding(16.dp)
                        } else {
                            Modifier.fillMaxWidth().padding(16.dp)
                        },
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = if (isPlayersScreen) {
                                Modifier.widthIn(max = 1040.dp).fillMaxSize()
                            } else {
                                Modifier.widthIn(max = 1040.dp)
                            }
                        ) {
                        AnimatedContent(
                            targetState = navigation.currentTopLevelScreen,
                            transitionSpec = {
                                val direction = if (
                                    topLevelScreenIndex(targetState) > topLevelScreenIndex(initialState)
                                ) 1 else -1

                                slideInHorizontally(
                                    initialOffsetX = { direction * it },
                                    animationSpec = tween(300)
                                ) togetherWith slideOutHorizontally(
                                    targetOffsetX = { -direction * it },
                                    animationSpec = tween(300)
                                )
                            },
                            label = "Bottom navigation transition"
                        ) { topLevelScreen ->
                            NavDisplay(
                            backStack = navigation.backStackFor(topLevelScreen),
                            onBack = navigation::navigateBack,
                            entryProvider = entryProvider {
                                entry<Screens.Home> {
                                    HomeScreen(
                                        viewModel = homeViewModel,
                                        onEvent = { event ->
                                            when (event) {
                                                HomeEvent.OpenAdmin -> navigation.navigate(Screens.Admin)
                                                HomeEvent.OpenHistory -> navigation.navigate(Screens.History)
                                                is HomeEvent.OpenPlayers -> {
                                                    playersViewModel.selectCategory(event.category)
                                                    navigation.navigate(Screens.Players)
                                                }
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
                                        modifier = Modifier.fillMaxSize(),
                                        onEvent = { event ->
                                            when (event) {
                                                is PlayersEvent.SelectCategory -> {
                                                    playersViewModel.selectCategory(event.category)
                                                }
                                                is PlayersEvent.SelectRankingView -> {
                                                    playersViewModel.selectRankingView(event.view)
                                                }
                                                is PlayersEvent.SelectPlayer -> Unit
                                            }
                                        }
                                    )
                                }

                                entry<Screens.Admin> {
                                    AdminScreen(
                                        viewModel = adminViewModel,
                                        appVersion = appVersion,
                                        onNewMatch = { navigation.navigate(Screens.NewMatch) },
                                        onAddPlayer = { navigation.navigate(Screens.NewPlayer) },
                                        onManageMatches = { navigation.navigate(Screens.ManageMatches) },
                                        onManagePlayers = { navigation.navigate(Screens.ManagePlayers) }
                                    )
                                }

                                entry<Screens.NewMatch> {
                                    val newMatchViewModel = remember(adminAuthRepository) {
                                        NewMatchViewModel(
                                            adminAuthRepository?.let { repository ->
                                                AdminMatchRepository(repository.client, repository)
                                            }
                                        )
                                    }
                                    NewMatchScreen(
                                        viewModel = newMatchViewModel,
                                        onSaved = navigation::navigateBack
                                    )
                                }

                                entry<Screens.NewPlayer> {
                                    val newPlayerViewModel = remember(adminAuthRepository) {
                                        NewPlayerViewModel(
                                            adminAuthRepository?.let { repository ->
                                                AdminPlayerRepository(repository.client, repository)
                                            }
                                        )
                                    }
                                    NewPlayerScreen(
                                        viewModel = newPlayerViewModel,
                                        onSaved = navigation::navigateBack
                                    )
                                }

                                entry<Screens.ManageMatches> {
                                    val manageMatchesViewModel = remember(adminAuthRepository) {
                                        ManageMatchesViewModel(
                                            adminAuthRepository?.let { repository ->
                                                AdminMatchRepository(repository.client, repository)
                                            }
                                        )
                                    }
                                    ManageMatchesScreen(
                                        viewModel = manageMatchesViewModel,
                                        onEdit = { matchId -> navigation.navigate(Screens.EditMatch(matchId)) }
                                    )
                                }

                                entry<Screens.ManagePlayers> {
                                    val managePlayersViewModel = remember(adminAuthRepository) {
                                        ManagePlayersViewModel(
                                            adminAuthRepository?.let { repository ->
                                                AdminPlayerRepository(repository.client, repository)
                                            }
                                        )
                                    }
                                    ManagePlayersScreen(
                                        viewModel = managePlayersViewModel,
                                        onEdit = { playerId -> navigation.navigate(Screens.EditPlayer(playerId)) }
                                    )
                                }

                                entry<Screens.EditMatch> { screen ->
                                    val editMatchViewModel = remember(adminAuthRepository, screen.matchId) {
                                        NewMatchViewModel(
                                            adminAuthRepository?.let { repository ->
                                                AdminMatchRepository(repository.client, repository)
                                            },
                                            matchId = screen.matchId
                                        )
                                    }
                                    NewMatchScreen(
                                        viewModel = editMatchViewModel,
                                        onSaved = navigation::navigateBack
                                    )
                                }

                                entry<Screens.EditPlayer> { screen ->
                                    val editPlayerViewModel = remember(adminAuthRepository, screen.playerId) {
                                        NewPlayerViewModel(
                                            adminAuthRepository?.let { repository ->
                                                AdminPlayerRepository(repository.client, repository)
                                            },
                                            playerId = screen.playerId
                                        )
                                    }
                                    NewPlayerScreen(
                                        viewModel = editPlayerViewModel,
                                        onSaved = navigation::navigateBack
                                    )
                                }

                                entry<Screens.MatchDetail> { screen ->
                                    val matchDetailViewModel = remember(screen.matchId, footballRepository) {
                                        MatchDetailViewModel(
                                            matchId = screen.matchId,
                                            repository = footballRepository
                                        )
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
}

private fun topLevelScreenIndex(screen: Screens): Int = when (screen) {
    Screens.Home -> 0
    Screens.History -> 1
    Screens.Players -> 2
    Screens.Admin -> 3
    else -> error("Only bottom-navigation screens can be animated as tabs: $screen")
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
    Screens.Players -> "Clasificaciones"
    Screens.Admin -> "Zona mister"
    Screens.NewMatch -> "Nuevo partido"
    Screens.NewPlayer -> "Añadir jugador"
    Screens.ManageMatches -> "Gestionar partidos"
    Screens.ManagePlayers -> "Gestionar jugadores"
    is Screens.EditMatch -> "Editar partido"
    is Screens.EditPlayer -> "Editar jugador"
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
            label = { Text("Clasificaciones") },
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
