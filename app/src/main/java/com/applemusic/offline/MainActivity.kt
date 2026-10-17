package com.applemusic.offline

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.applemusic.offline.ui.library.LibraryScreen
import com.applemusic.offline.ui.player.PlayerScreen
import com.applemusic.offline.ui.search.SearchScreen
import com.applemusic.offline.ui.theme.AppleMusicOfflineTheme
import com.applemusic.offline.ui.theme.AppleRed
import com.applemusic.offline.viewmodel.PlayerViewModel
import com.applemusic.offline.viewmodel.PlaybackState
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
                val isDarkTheme = isSystemInDarkTheme()

                LaunchedEffect(isDarkTheme) {
                    val window = window
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)

                    insetsController.isAppearanceLightStatusBars = !isDarkTheme
                    window.statusBarColor = if (isDarkTheme) 
                        android.graphics.Color.BLACK 
                    else 
                        android.graphics.Color.WHITE

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
    val backgroundColor = if (isDarkTheme) Color(0xFF000000) else Color(0xFFFFFFFF)
    val surfaceColor = if (isDarkTheme) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
    val textColor = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF000000)
    val secondaryTextColor = Color(0xFF8E8E93)

    val currentSong by playerViewModel.currentSong.collectAsState()
    val playbackState by playerViewModel.playbackState.collectAsState()

    val items = listOf(
        Screen.Library,
        Screen.Search
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                Column {
                    // Mini Player
                    if (currentSong != null && !isPlayerVisible) {
                        MiniPlayer(
                            song = currentSong!!,
                            isPlaying = playbackState is PlaybackState.Playing,
                            onPlayPause = { playerViewModel.playPause() },
                            onClick = { isPlayerVisible = true },
                            surfaceColor = surfaceColor,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor
                        )
                    }

                    // Navigation Bar
                    NavigationBar(
                        containerColor = surfaceColor,
                        contentColor = textColor,
                        tonalElevation = 0.dp // Remove elevação que causa sombra
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
                                    indicatorColor = Color.Transparent // SEM círculo de fundo
                                )
                            )
                        }
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

@Composable
fun MiniPlayer(
    song: com.applemusic.offline.data.model.Song,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onClick: () -> Unit,
    surfaceColor: Color,
    textColor: Color,
    secondaryTextColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = surfaceColor,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(secondaryTextColor.copy(alpha = 0.3f))
            ) {
                if (song.albumArtUri != null) {
                    AsyncImage(
                        model = song.albumArtUri,
                        contentDescription = "Album art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = secondaryTextColor,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = textColor
                )
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Library : Screen("library", "Biblioteca", Icons.Default.LibraryMusic)
    object Search : Screen("search", "Buscar", Icons.Default.Search)
}
