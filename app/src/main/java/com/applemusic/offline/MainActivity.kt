package com.applemusic.offline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.applemusic.offline.ui.library.LibraryScreen
import com.applemusic.offline.ui.player.PlayerScreen
import com.applemusic.offline.ui.theme.AppleMusicOfflineTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppleMusicOfflineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "library") {
        composable("library") {
            LibraryScreen(
                onSongClick = { song, songs, index ->
                    // Iniciar reprodução e navegar para o player
                    navController.navigate("player")
                },
                onNavigateToPlayer = {
                    navController.navigate("player")
                }
            )
        }
        composable("player") {
            PlayerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
