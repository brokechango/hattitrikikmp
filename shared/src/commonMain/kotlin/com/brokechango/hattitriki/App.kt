package com.brokechango.hattitriki

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokechango.hattitriki.core.design.CrestBlack
import com.brokechango.hattitriki.core.design.CrestGold
import com.brokechango.hattitriki.core.design.CrestNavy
import com.brokechango.hattitriki.core.design.CrestWhite
import com.brokechango.hattitriki.core.auth.AuthRepository
import com.brokechango.hattitriki.core.auth.LeagueRole
import com.brokechango.hattitriki.core.data.AdminMatchRepository
import com.brokechango.hattitriki.core.data.AdminPlayerRepository
import com.brokechango.hattitriki.core.data.LeagueInvitationRepository
import com.brokechango.hattitriki.core.data.MultiplatformSettingsMatchTeamsDraftStore
import com.brokechango.hattitriki.core.data.SupabasePlayerProfileRepository
import com.brokechango.hattitriki.core.data.SupabaseFriendlyFootballRepository
import com.brokechango.hattitriki.core.design.HattitrikiTheme
import com.brokechango.hattitriki.ui.composables.PitchBackground
import com.brokechango.hattitriki.ui.preview.PreviewTargets
import com.brokechango.hattitriki.core.navigation.HattitrikiTab
import com.brokechango.hattitriki.core.navigation.Screens
import com.brokechango.hattitriki.core.navigation.HattitrikiNavigationState
import com.brokechango.hattitriki.core.navigation.rememberHattitrikiNavigationState
import com.brokechango.hattitriki.feature.admin.AdminScreen
import com.brokechango.hattitriki.feature.auth.AuthEvent
import com.brokechango.hattitriki.feature.auth.AuthGateState
import com.brokechango.hattitriki.feature.auth.AuthScreen
import com.brokechango.hattitriki.feature.auth.AuthViewModel
import com.brokechango.hattitriki.feature.history.HistoryEvent
import com.brokechango.hattitriki.feature.history.HistoryScreen
import com.brokechango.hattitriki.feature.history.HistoryViewModel
import com.brokechango.hattitriki.feature.home.HomeEvent
import com.brokechango.hattitriki.feature.home.HomeScreen
import com.brokechango.hattitriki.feature.home.HomeViewModel
import com.brokechango.hattitriki.feature.home.MultiplatformSettingsHomeStatsOrderStore
import com.brokechango.hattitriki.feature.invitation.LeagueInvitationScreen
import com.brokechango.hattitriki.feature.invitation.LeagueInvitationViewModel
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
import com.brokechango.hattitriki.feature.teamrandomizer.TeamRandomizerResultScreen
import com.brokechango.hattitriki.feature.teamrandomizer.TeamRandomizerScreen
import com.brokechango.hattitriki.feature.teamrandomizer.TeamRandomizerViewModel
import com.brokechango.hattitriki.feature.players.PlayersScreen
import com.brokechango.hattitriki.feature.players.PlayersEvent
import com.brokechango.hattitriki.feature.players.PlayersViewModel
import com.brokechango.hattitriki.feature.playerprofile.PlayerProfileScreen
import com.brokechango.hattitriki.feature.playerprofile.PlayerProfileViewModel
import com.brokechango.hattitriki.feature.settings.SettingsScreen
import com.brokechango.hattitriki.feature.settings.SettingsViewModel
import hattitriki.shared.generated.resources.Res
import hattitriki.shared.generated.resources.*
import hattitriki.shared.generated.resources.hattitriki_app_icon
import hattitriki.shared.generated.resources.icon_home
import hattitriki.shared.generated.resources.icon_matches
import hattitriki.shared.generated.resources.icon_rankings
import hattitriki.shared.generated.resources.icon_settings
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
@Preview
@PreviewTargets
@OptIn(ExperimentalMaterial3Api::class)
fun App(
    authRepository: AuthRepository? = null,
    appVersion: String = "1.0",
    initialAuthEmail: String = "",
    initialAuthPassword: String = "",
    submitInitialAuth: Boolean = false,
    browserNavigationEffect: (@Composable (HattitrikiNavigationState) -> Unit)? = null
) {
    HattitrikiTheme {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val authViewModel = remember(authRepository) {
            AuthViewModel(
                authRepository = authRepository,
                initialEmail = initialAuthEmail,
                initialPassword = initialAuthPassword,
                submitInitialLogin = submitInitialAuth
            )
        }
        val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
        val access = (authUiState.gateState as? AuthGateState.Authenticated)?.access

        if (access == null) {
            AuthScreen(
                uiState = authUiState,
                onEvent = authViewModel::onEvent
            )
            return@BoxWithConstraints
        }

        val activeAuthRepository = checkNotNull(authRepository)
        val isAdmin = access.role == LeagueRole.ADMIN
        val useDesktopWebLayout = getPlatform().name == "Web" && maxWidth >= 980.dp
        val navigation = rememberHattitrikiNavigationState(isAdmin)
        browserNavigationEffect?.invoke(navigation)
        val currentScreen = navigation.currentScreen
        val currentTopLevelScreen = navigation.currentTopLevelScreen
        val canNavigateWithinTab = navigation.canNavigateBack && currentScreen != currentTopLevelScreen
        val homeScrollState = rememberScrollState()
        val historyScrollState = rememberScrollState()
        val adminScrollState = rememberScrollState()

        val footballRepository = remember(activeAuthRepository) {
            SupabaseFriendlyFootballRepository(activeAuthRepository.client)
        }
        val playerProfileRepository = remember(activeAuthRepository) {
            SupabasePlayerProfileRepository(activeAuthRepository.client)
        }
        val homeStatsOrderStore = remember {
            MultiplatformSettingsHomeStatsOrderStore(settings)
        }
        val matchTeamsDraftStore = remember {
            MultiplatformSettingsMatchTeamsDraftStore(settings)
        }
        val homeViewModel = remember(
            footballRepository,
            homeStatsOrderStore,
            playerProfileRepository
        ) {
            HomeViewModel(
                repository = footballRepository,
                statsOrderStore = homeStatsOrderStore,
                profileRepository = playerProfileRepository
            )
        }
        val historyViewModel = remember(footballRepository) { HistoryViewModel(footballRepository) }
        val playersViewModel = remember(footballRepository, playerProfileRepository) {
            PlayersViewModel(footballRepository, playerProfileRepository)
        }
        val currentTeamRandomizerOpenTime = when (currentScreen) {
            is Screens.TeamRandomizer -> currentScreen.openTime
            is Screens.TeamRandomizerResult -> currentScreen.openTime
            else -> null
        }
        val lastTeamRandomizerOpenTime = remember { mutableStateOf<Long?>(null) }
        currentTeamRandomizerOpenTime?.let { openTime ->
            SideEffect {
                lastTeamRandomizerOpenTime.value = openTime
            }
        }
        // NavDisplay keeps a destination composed until its pop animation completes. Retain
        // the randomizer session key during that short interval so the outgoing screen does
        // not lose its ViewModel when navigating back to the admin screen.
        val activeTeamRandomizerOpenTime =
            currentTeamRandomizerOpenTime ?: lastTeamRandomizerOpenTime.value
        val teamRandomizerViewModel = remember(
            activeAuthRepository,
            footballRepository,
            matchTeamsDraftStore,
            activeTeamRandomizerOpenTime
        ) {
            activeTeamRandomizerOpenTime?.let {
                TeamRandomizerViewModel(
                    adminPlayerRepository = AdminPlayerRepository(
                        activeAuthRepository.client,
                        activeAuthRepository
                    ),
                    footballRepository = footballRepository,
                    matchTeamsDraftStore = matchTeamsDraftStore
                )
            }
        }

        LaunchedEffect(currentTopLevelScreen) {
            when (currentTopLevelScreen) {
                Screens.Home -> homeScrollState.scrollTo(0)
                Screens.History -> historyScrollState.scrollTo(0)
                Screens.Admin -> adminScrollState.scrollTo(0)
                is Screens.Players -> Unit
                else -> error("Only top-level screens can be selected as tabs")
            }
        }

        LaunchedEffect(currentScreen) {
            if (currentTopLevelScreen == Screens.Admin) {
                adminScrollState.scrollTo(0)
            }
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            topBar = {
                if (useDesktopWebLayout) {
                    DesktopWebTopBar(
                        currentScreen = currentScreen,
                        currentTopLevelScreen = currentTopLevelScreen,
                        canNavigateBack = canNavigateWithinTab,
                        isAdmin = isAdmin,
                        onBack = navigation::navigateBack,
                        onNavigate = navigation::selectTopLevel,
                        onOpenSettings = { navigation.navigate(Screens.Settings) },
                        onLogout = { authViewModel.onEvent(AuthEvent.Logout) }
                    )
                } else {
                    MatchTopBar(
                        currentScreen = currentScreen,
                        canNavigateBack = canNavigateWithinTab,
                        onBack = navigation::navigateBack,
                        onOpenSettings = { navigation.navigate(Screens.Settings) },
                        onLogout = { authViewModel.onEvent(AuthEvent.Logout) }
                    )
                }
            },
            bottomBar = {
                if (!useDesktopWebLayout) {
                    MainNavigationBar(
                        currentScreen = currentTopLevelScreen,
                        isAdmin = isAdmin,
                        onNavigate = navigation::selectTopLevel
                    )
                }
            }
            ) { innerPadding ->
                PitchBackground(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = if (useDesktopWebLayout) 28.dp else 16.dp,
                                vertical = if (useDesktopWebLayout) 24.dp else 16.dp
                            ),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = if (useDesktopWebLayout) 1180.dp else 1040.dp)
                                .fillMaxSize()
                        ) {
                            AnimatedContent(
                                targetState = currentTopLevelScreen,
                                transitionSpec = {
                                    if (useDesktopWebLayout) {
                                        fadeIn(tween(180)) togetherWith fadeOut(tween(120))
                                    } else {
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
                                    }
                                },
                                label = "Bottom navigation transition"
                            ) { topLevelScreen ->
                                val scrollState = when (topLevelScreen) {
                                    Screens.Home, Screens.History -> null
                                    Screens.Admin -> adminScrollState
                                    is Screens.Players -> null
                                    else -> error("Only top-level screens can be animated as tabs")
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .then(
                                            scrollState?.let { Modifier.verticalScroll(it) } ?: Modifier
                                        )
                                ) {
                                    NavDisplay(
                                        backStack = navigation.backStackFor(topLevelScreen),
                                        onBack = navigation::navigateBack,
                                        entryProvider = entryProvider {
                                entry<Screens.Home> {
                                    HomeScreen(
                                        viewModel = homeViewModel,
                                        scrollState = homeScrollState,
                                        onEvent = { event ->
                                            when (event) {
                                                HomeEvent.OpenAdmin -> if (isAdmin) {
                                                    navigation.navigate(Screens.Admin)
                                                }
                                                HomeEvent.OpenHistory -> navigation.navigate(Screens.History)
                                                is HomeEvent.OpenPlayers -> {
                                                    playersViewModel.selectCategory(event.category)
                                                    navigation.selectTopLevel(HattitrikiTab.Players)
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
                                        scrollState = historyScrollState,
                                        onEvent = { event ->
                                            when (event) {
                                                is HistoryEvent.OpenMatch -> navigation.navigate(
                                                    Screens.MatchDetail(event.matchId)
                                                )
                                            }
                                        }
                                    )
                                }

                                entry<Screens.Players> { screen ->
                                    PlayersScreen(
                                        viewModel = playersViewModel,
                                        openTime = screen.openTime,
                                        modifier = Modifier.fillMaxSize(),
                                        onEvent = { event ->
                                            when (event) {
                                                is PlayersEvent.SelectCategory -> {
                                                    playersViewModel.selectCategory(event.category)
                                                }
                                                is PlayersEvent.SelectRankingView -> {
                                                    playersViewModel.selectRankingView(event.view)
                                                }
                                                is PlayersEvent.SelectPlayer -> navigation.navigate(
                                                    Screens.PlayerProfile(event.playerId)
                                                )
                                            }
                                        }
                                    )
                                }

                                entry<Screens.Admin> {
                                    if (isAdmin) AdminScreen(
                                        appVersion = appVersion,
                                        onNewMatch = {
                                            navigation.navigate(
                                                Screens.NewMatch(
                                                    openTime = kotlin.time.Clock.System.now()
                                                        .toEpochMilliseconds()
                                                )
                                            )
                                        },
                                        onAddPlayer = { navigation.navigate(Screens.NewPlayer) },
                                        onManageMatches = { navigation.navigate(Screens.ManageMatches) },
                                        onManagePlayers = { navigation.navigate(Screens.ManagePlayers) },
                                        onInviteLeagueMember = {
                                            navigation.navigate(Screens.LeagueInvitation)
                                        },
                                        onTeamRandomizer = {
                                            navigation.navigate(
                                                Screens.TeamRandomizer(
                                                    openTime = kotlin.time.Clock.System.now()
                                                        .toEpochMilliseconds()
                                                )
                                            )
                                        }
                                    )
                                }

                                entry<Screens.NewMatch> { screen ->
                                    val newMatchViewModel = remember(activeAuthRepository, screen.openTime) {
                                        NewMatchViewModel(
                                            AdminMatchRepository(
                                                activeAuthRepository.client,
                                                activeAuthRepository
                                            ),
                                            matchTeamsDraftStore = matchTeamsDraftStore
                                        )
                                    }
                                    NewMatchScreen(
                                        viewModel = newMatchViewModel,
                                        onSaved = navigation::navigateBack
                                    )
                                }

                                entry<Screens.NewPlayer> {
                                    val newPlayerViewModel = remember(activeAuthRepository) {
                                        NewPlayerViewModel(
                                            AdminPlayerRepository(
                                                activeAuthRepository.client,
                                                activeAuthRepository
                                            )
                                        )
                                    }
                                    NewPlayerScreen(
                                        viewModel = newPlayerViewModel,
                                        onSaved = navigation::navigateBack
                                    )
                                }

                                entry<Screens.ManageMatches> {
                                    val manageMatchesViewModel = remember(activeAuthRepository) {
                                        ManageMatchesViewModel(
                                            AdminMatchRepository(
                                                activeAuthRepository.client,
                                                activeAuthRepository
                                            )
                                        )
                                    }
                                    ManageMatchesScreen(
                                        viewModel = manageMatchesViewModel,
                                        onEdit = { matchId -> navigation.navigate(Screens.EditMatch(matchId)) }
                                    )
                                }

                                entry<Screens.ManagePlayers> {
                                    val managePlayersViewModel = remember(activeAuthRepository) {
                                        ManagePlayersViewModel(
                                            AdminPlayerRepository(
                                                activeAuthRepository.client,
                                                activeAuthRepository
                                            )
                                        )
                                    }
                                    ManagePlayersScreen(
                                        viewModel = managePlayersViewModel,
                                        onEdit = { playerId -> navigation.navigate(Screens.EditPlayer(playerId)) }
                                    )
                                }

                                entry<Screens.LeagueInvitation> {
                                    val invitationViewModel = remember(activeAuthRepository) {
                                        LeagueInvitationViewModel(
                                            LeagueInvitationRepository(
                                                activeAuthRepository.client,
                                                activeAuthRepository
                                            )
                                        )
                                    }
                                    LeagueInvitationScreen(viewModel = invitationViewModel)
                                }

                                entry<Screens.PlayerProfile> { screen ->
                                    val playerProfileViewModel = remember(
                                        screen.playerId,
                                        footballRepository,
                                        playerProfileRepository
                                    ) {
                                        PlayerProfileViewModel(
                                            playerId = screen.playerId,
                                            footballRepository = footballRepository,
                                            profileRepository = playerProfileRepository
                                        )
                                    }
                                    PlayerProfileScreen(
                                        viewModel = playerProfileViewModel,
                                        onPlayerSelected = { playerId ->
                                            navigation.navigate(Screens.PlayerProfile(playerId))
                                        }
                                    )
                                }

                                entry<Screens.Settings> {
                                    val settingsViewModel = remember(
                                        footballRepository,
                                        playerProfileRepository
                                    ) {
                                        SettingsViewModel(
                                            footballRepository = footballRepository,
                                            profileRepository = playerProfileRepository
                                        )
                                    }
                                    SettingsScreen(
                                        viewModel = settingsViewModel,
                                        accountEmail = access.email
                                    )
                                }

                                entry<Screens.TeamRandomizer> { screen ->
                                    TeamRandomizerScreen(
                                        viewModel = checkNotNull(teamRandomizerViewModel),
                                        onResultReady = {
                                            navigation.navigate(
                                                Screens.TeamRandomizerResult(
                                                    openTime = screen.openTime
                                                )
                                            )
                                        }
                                    )
                                }

                                entry<Screens.TeamRandomizerResult> {
                                    TeamRandomizerResultScreen(
                                        viewModel = checkNotNull(teamRandomizerViewModel),
                                        onBack = navigation::navigateBack
                                    )
                                }

                                entry<Screens.EditMatch> { screen ->
                                    val editMatchViewModel = remember(activeAuthRepository, screen.matchId) {
                                        NewMatchViewModel(
                                            AdminMatchRepository(
                                                activeAuthRepository.client,
                                                activeAuthRepository
                                            ),
                                            matchId = screen.matchId
                                        )
                                    }
                                    NewMatchScreen(
                                        viewModel = editMatchViewModel,
                                        onSaved = navigation::navigateBack
                                    )
                                }

                                entry<Screens.EditPlayer> { screen ->
                                    val editPlayerViewModel = remember(activeAuthRepository, screen.playerId) {
                                        NewPlayerViewModel(
                                            AdminPlayerRepository(
                                                activeAuthRepository.client,
                                                activeAuthRepository
                                            ),
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
    }
}

private fun topLevelScreenIndex(screen: Screens): Int = when (screen) {
    Screens.Home -> 0
    Screens.History -> 1
    is Screens.Players -> 2
    Screens.Admin -> 3
    else -> error("Only bottom-navigation screens can be animated as tabs: $screen")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchTopBar(
    currentScreen: Screens,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = if (currentScreen == Screens.Home) stringResource(Res.string.app_title_uppercase) else topBarTitle(
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
                        contentDescription = stringResource(Res.string.content_description_app_logo),
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        },
        actions = {
            AccountMenu(onOpenSettings = onOpenSettings, onLogout = onLogout)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CrestNavy,
            titleContentColor = CrestWhite,
            navigationIconContentColor = CrestGold,
            actionIconContentColor = CrestWhite
        )
    )
}

@Composable
private fun topBarTitle(screen: Screens): String = when (screen) {
    is Screens.PlayerProfile -> "Perfil de jugador"
    Screens.Settings -> "Ajustes"
    else -> stringResource(when (screen) {
        Screens.Home -> Res.string.app_title
        Screens.History -> Res.string.top_bar_history
        is Screens.Players -> Res.string.navigation_rankings
        Screens.Admin -> Res.string.navigation_manager_area
        is Screens.NewMatch -> Res.string.top_bar_new_match
        Screens.NewPlayer -> Res.string.top_bar_new_player
        Screens.ManageMatches -> Res.string.top_bar_manage_matches
        Screens.ManagePlayers -> Res.string.top_bar_manage_players
        Screens.LeagueInvitation -> Res.string.top_bar_invitation
        is Screens.TeamRandomizer -> Res.string.top_bar_team_randomizer
        is Screens.TeamRandomizerResult -> Res.string.top_bar_team_randomizer_result
        is Screens.EditMatch -> Res.string.top_bar_edit_match
        is Screens.EditPlayer -> Res.string.top_bar_edit_player
        is Screens.MatchDetail -> Res.string.top_bar_match_record
        is Screens.PlayerProfile -> error("Handled before resource lookup")
        Screens.Settings -> error("Handled before resource lookup")
    })
}

@Composable
private fun AccountMenu(onOpenSettings: () -> Unit, onLogout: () -> Unit) {
    val expanded = remember { mutableStateOf(false) }

    Box {
        TextButton(
            onClick = { expanded.value = true },
            colors = ButtonDefaults.textButtonColors(contentColor = CrestWhite)
        ) {
            Text("MENÚ", fontWeight = FontWeight.Bold)
        }
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            DropdownMenuItem(
                text = { Text("Ajustes") },
                onClick = {
                    expanded.value = false
                    onOpenSettings()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.action_sign_out)) },
                onClick = {
                    expanded.value = false
                    onLogout()
                }
            )
        }
    }
}

@Composable
private fun MainNavigationBar(
    currentScreen: Screens,
    isAdmin: Boolean,
    onNavigate: (HattitrikiTab) -> Unit
) {
    NavigationBar(
        containerColor = CrestNavy,
        contentColor = CrestWhite,
        tonalElevation = NavigationBarDefaults.Elevation
    ) {
        NavigationBarItem(
            selected = currentScreen == Screens.Home,
            onClick = { onNavigate(HattitrikiTab.Home) },
            label = { Text(stringResource(Res.string.navigation_home)) },
            icon = {
                NavigationIcon(
                    resource = Res.drawable.icon_home,
                    contentDescription = stringResource(Res.string.navigation_home)
                )
            },
            colors = navItemColors()
        )
        NavigationBarItem(
            selected = currentScreen == Screens.History,
            onClick = { onNavigate(HattitrikiTab.History) },
            label = { Text(stringResource(Res.string.navigation_matches)) },
            icon = {
                NavigationIcon(
                    resource = Res.drawable.icon_matches,
                    contentDescription = stringResource(Res.string.navigation_matches)
                )
            },
            colors = navItemColors()
        )
        NavigationBarItem(
            selected = currentScreen is Screens.Players,
            onClick = { onNavigate(HattitrikiTab.Players) },
            label = { Text(stringResource(Res.string.navigation_rankings)) },
            icon = {
                NavigationIcon(
                    resource = Res.drawable.icon_rankings,
                    contentDescription = stringResource(Res.string.navigation_rankings)
                )
            },
            colors = navItemColors()
        )
        if (isAdmin) {
            NavigationBarItem(
                selected = currentScreen == Screens.Admin,
                onClick = { onNavigate(HattitrikiTab.Admin) },
                label = { Text(stringResource(Res.string.navigation_manager)) },
                icon = {
                    NavigationIcon(
                        resource = Res.drawable.icon_settings,
                        contentDescription = stringResource(Res.string.navigation_manager)
                    )
                },
                colors = navItemColors()
            )
        }
        NavigationBarItem(
            selected = currentScreen == Screens.Settings,
            onClick = { onNavigate(HattitrikiTab.Profile) },
            label = { Text(stringResource(Res.string.navigation_profile)) },
            icon = {
                NavigationIcon(
                    resource = Res.drawable.icon_profile,
                    contentDescription = stringResource(Res.string.navigation_profile)
                )
            },
            colors = navItemColors()
        )
    }
}

@Composable
private fun NavigationIcon(resource: DrawableResource, contentDescription: String) {
    Icon(
        imageVector = vectorResource(resource),
        contentDescription = contentDescription,
        modifier = Modifier.size(24.dp)
    )
}

private data class DesktopDestination(
    val tab: HattitrikiTab,
    val label: StringResource
)

private val desktopDestinations = listOf(
    DesktopDestination(HattitrikiTab.Home, Res.string.navigation_home),
    DesktopDestination(HattitrikiTab.History, Res.string.navigation_matches),
    DesktopDestination(HattitrikiTab.Players, Res.string.navigation_rankings)
)

private val desktopAdminDestination = DesktopDestination(
    HattitrikiTab.Admin,
    Res.string.navigation_manager_area
)

@Composable
private fun DesktopWebTopBar(
    currentScreen: Screens,
    currentTopLevelScreen: Screens,
    canNavigateBack: Boolean,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onNavigate: (HattitrikiTab) -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CrestNavy)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 1240.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onNavigate(HattitrikiTab.Home) },
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = CrestWhite)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.hattitriki_app_icon),
                            contentDescription = stringResource(Res.string.content_description_app_logo),
                            modifier = Modifier.size(36.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = stringResource(Res.string.app_title_uppercase),
                                style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = stringResource(Res.string.app_league_name_uppercase),
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                color = CrestGold,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.7.sp
                            )
                        }
                    }
                }

                if (canNavigateBack) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = 1.dp, height = 28.dp)
                            .background(CrestWhite.copy(alpha = 0.2f))
                    )
                    TextButton(
                        onClick = onBack,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = CrestGold)
                    ) {
                        Text(stringResource(Res.string.action_back), fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = topBarTitle(currentScreen),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                        color = CrestWhite.copy(alpha = 0.78f),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                val visibleDestinations = if (isAdmin) {
                    desktopDestinations + desktopAdminDestination
                } else {
                    desktopDestinations
                }
                visibleDestinations.forEach { destination ->
                    val selected = HattitrikiTab.from(currentTopLevelScreen) == destination.tab
                    TextButton(
                        onClick = { onNavigate(destination.tab) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (selected) CrestGold else Color.Transparent,
                            contentColor = if (selected) CrestBlack else CrestWhite.copy(alpha = 0.78f)
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = stringResource(destination.label),
                            fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold
                        )
                    }
                }

                AccountMenu(onOpenSettings = onOpenSettings, onLogout = onLogout)
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = CrestGold.copy(alpha = 0.65f)
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
