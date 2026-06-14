package com.example.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.ViewPipeApplication
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.home.HomeViewModel

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Outlined.Home, Icons.Filled.Home)
    object Shorts : Screen("shorts", "Shorts", Icons.Outlined.PlayArrow, Icons.Filled.PlayArrow)
    object Subscriptions : Screen("subscriptions", "Subscriptions", Icons.Outlined.Subscriptions, Icons.Filled.Subscriptions)
    object Library : Screen("library", "Library", Icons.Outlined.VideoLibrary, Icons.Filled.VideoLibrary)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Shorts,
    Screen.Subscriptions,
    Screen.Library
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(app: ViewPipeApplication) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Create ViewModels manually for now
    val homeViewModel = remember { HomeViewModel(app.youTubeRepository) }

    var activeVideoId by remember { mutableStateOf<String?>(null) }
    var isMiniPlayer by remember { mutableStateOf(false) }

    val isFullScreen = (activeVideoId != null && !isMiniPlayer) || currentRoute == "search"

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (!isFullScreen) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(width = 32.dp, height = 24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "NewPipe", 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 18.sp, 
                                letterSpacing = (-0.5).sp
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(imageVector = Icons.Outlined.Cast, contentDescription = "Cast")
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(imageVector = Icons.Outlined.Notifications, contentDescription = "Notifications")
                        }
                        IconButton(onClick = { navController.navigate("search") }) {
                            Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = androidx.compose.ui.graphics.Color(0xFF6366F1), // indigo-500
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("JD", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White)
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        bottomBar = {
            if (!isFullScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title, fontSize = 10.sp) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onBackground,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedTextColor = MaterialTheme.colorScheme.onBackground,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onVideoClick = { videoId ->
                        activeVideoId = videoId
                        isMiniPlayer = false
                    }
                )
            }
            composable("search") {
                com.example.ui.screens.search.SearchScreen(
                    repository = app.youTubeRepository,
                    onNavigateUp = { navController.navigateUp() },
                    onVideoClick = { videoId ->
                        activeVideoId = videoId
                        isMiniPlayer = false
                    }
                )
            }
            composable(Screen.Shorts.route) {
                com.example.ui.screens.shorts.ShortsScreen()
            }
            composable(Screen.Subscriptions.route) {
                com.example.ui.screens.subscriptions.SubscriptionsScreen(app.localRepository)
            }
            composable(Screen.Library.route) {
                com.example.ui.screens.library.LibraryScreen(
                    localRepository = app.localRepository,
                    onVideoClick = { videoId ->
                        activeVideoId = videoId
                        isMiniPlayer = false
                    }
                )
            }
        }
    }
    
    // Full Screen Player or MiniPlayer overlay
    activeVideoId?.let { videoId ->
        com.example.ui.screens.watch.WatchScreen(
            videoId = videoId,
            isMiniPlayer = isMiniPlayer,
            onNavigateUp = { isMiniPlayer = true },
            onClose = { activeVideoId = null; isMiniPlayer = false },
            onMaximize = { isMiniPlayer = false },
            localRepository = app.localRepository
        )
    }
}
}
