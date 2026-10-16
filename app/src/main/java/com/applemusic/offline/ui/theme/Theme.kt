package com.applemusic.offline.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Cores do Apple Music (do documento de análise)
val AppleRed = Color(0xFFFA233B)
val ApplePink = Color(0xFFFF2D55)
val AppleGray = Color(0xFF8E8E93)

// Cores escuras
val DarkBackground = Color(0xFF1C1C1E)
val DarkSurface = Color(0xFF2C2C2E)
val DarkOnBackground = Color(0xFFFFFFFF)
val DarkOnSurface = Color(0xFFFFFFFF)

// Cores claras
val LightBackground = Color(0xFFF2F2F7)
val LightSurface = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xFF000000)
val LightOnSurface = Color(0xFF000000)

// Esquema de cores escuro
private val DarkColorScheme = darkColorScheme(
    primary = AppleRed,
    secondary = ApplePink,
    tertiary = AppleGray,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    onPrimary = Color.White,
    onSecondary = Color.White
)

// Esquema de cores claro
private val LightColorScheme = lightColorScheme(
    primary = AppleRed,
    secondary = ApplePink,
    tertiary = AppleGray,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onPrimary = Color.White,
    onSecondary = Color.White
)

@Composable
fun AppleMusicOfflineTheme(
    darkTheme: Boolean = true, // Apple Music é predominantemente escuro
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
