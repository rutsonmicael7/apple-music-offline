package com.applemusic.offline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.applemusic.offline.ui.library.LibraryScreen
import com.applemusic.offline.ui.player.PlayerScreen
import com.applemusic.offline.ui.search.SearchScreen
import com.applemusic.offline.ui.theme.AppleMusicOfflineTheme
import com.applemusic.offline.ui.theme.AppleRed
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppleMusicOfflineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    AppleMusicApp()
                }
            }
        }
    }
}

@Composable
fun AppleMusicApp() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Library,
        Screen.Search,
        Screen.Player
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1C1C1E),
                contentColor = Color.White
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        selected = currentDestination?.route == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppleRed,
                            selectedTextColor = AppleRed,
                            unselectedIconColor = Color(0xFF8E8E93),
                            unselectedTextColor = Color(0xFF8E8E93),
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Library.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Library.route) {
                LibraryScreen(
                    onSongClick = { song, songs, index ->
                        navController.navigate(Screen.Player.route)
                    },
                    onNavigateToPlayer = {
                        navController.navigate(Screen.Player.route)
                    }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onSongClick = { song, songs, index ->
                        navController.navigate(Screen.Player.route)
                    }
                )
            }
            composable(Screen.Player.route) {
                PlayerScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Library : Screen("library", "Biblioteca", Icons.Default.LibraryMusic)
    object Search : Screen("search", "Buscar", Icons.Default.Search)
    object Player : Screen("player", "Tocando", Icons.Default.PlayCircle)
}
