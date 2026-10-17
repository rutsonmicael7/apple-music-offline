package com.applemusic.offline

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.applemusic.offline.viewmodel.LibraryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permissão concedida, app vai escanear automaticamente
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestStoragePermission()

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

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                // Já tem permissão
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
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
        AnimatedNavHost(
            navController = navController,
            startDestination = Screen.Library.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(
                Screen.Library.route,
                enterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) }
            ) {
                LibraryScreen(
                    onSongClick = { song, songs, index ->
                        navController.navigate(Screen.Player.route)
                    },
                    onNavigateToPlayer = {
                        navController.navigate(Screen.Player.route)
                    }
                )
            }
            composable(
                Screen.Search.route,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) }
            ) {
                SearchScreen(
                    onSongClick = { song, songs, index ->
                        navController.navigate(Screen.Player.route)
                    }
                )
            }
            composable(
                Screen.Player.route,
                enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)) },
                exitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400)) },
                popEnterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)) },
                popExitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400)) }
            ) {
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
