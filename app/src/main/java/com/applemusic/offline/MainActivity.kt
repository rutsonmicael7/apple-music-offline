package com.applemusic.offline

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.core.view.WindowCompat
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
import com.applemusic.offline.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permissão concedida
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestStoragePermission()

        setContent {
            AppleMusicOfflineTheme {
                // Detecta tema para configurar barras do sistema
                val isDarkTheme = isSystemInDarkTheme()

                // Configura barras do sistema (status bar e navigation bar)
                LaunchedEffect(isDarkTheme) {
                    val window = window
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)

                    // Status bar (barra de cima com hora, bateria)
                    insetsController.isAppearanceLightStatusBars = !isDarkTheme
                    window.statusBarColor = if (isDarkTheme) 
                        android.graphics.Color.BLACK 
                    else 
                        android.graphics.Color.WHITE

                    // Navigation bar (barra de baixo com gestos)
                    insetsController.isAppearanceLightNavigationBars = !isDarkTheme
                    window.navigationBarColor = if (isDarkTheme) 
                        android.graphics.Color.BLACK 
                    else 
                        android.graphics.Color.WHITE
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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

@Composable
fun AppleMusicApp() {
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = hiltViewModel()

    var isPlayerVisible by remember { mutableStateOf(false) }

    val isDarkTheme = isSystemInDarkTheme()

    // Cores adaptativas
    val backgroundColor = if (isDarkTheme) Color(0xFF000000) else Color(0xFFFFFFFF)
    val surfaceColor = if (isDarkTheme) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
    val textColor = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF000000)
    val secondaryTextColor = Color(0xFF8E8E93)

    val items = listOf(
        Screen.Library,
        Screen.Search
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = surfaceColor,
                    contentColor = textColor
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
                                unselectedIconColor = secondaryTextColor,
                                unselectedTextColor = secondaryTextColor,
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
                            playerViewModel.playSongs(songs, index)
                            isPlayerVisible = true
                        }
                    )
                }
                composable(Screen.Search.route) {
                    SearchScreen(
                        onSongClick = { song, songs, index ->
                            playerViewModel.playSongs(songs, index)
                            isPlayerVisible = true
                        }
                    )
                }
            }
        }

        // Player overlay
        if (isPlayerVisible) {
            PlayerScreen(
                viewModel = playerViewModel,
                onNavigateBack = {
                    isPlayerVisible = false
                }
            )
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Library : Screen("library", "Biblioteca", Icons.Default.LibraryMusic)
    object Search : Screen("search", "Buscar", Icons.Default.Search)
}
