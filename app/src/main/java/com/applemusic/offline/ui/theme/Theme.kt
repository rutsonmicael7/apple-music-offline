package com.applemusic.offline.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Cores do Apple Music
val AppleRed = Color(0xFFFA233B)
val ApplePink = Color(0xFFFF2D55)
val AppleGray = Color(0xFF8E8E93)

// Cores escuras (modo escuro padrão)
val DarkBackground = Color(0xFF000000)
val DarkSurface = Color(0xFF1C1C1E)
val DarkOnBackground = Color(0xFFFFFFFF)
val DarkOnSurface = Color(0xFFFFFFFF)
val DarkSecondary = Color(0xFF8E8E93)

// Cores claras (modo claro)
val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFF2F2F7)
val LightOnBackground = Color(0xFF000000)
val LightOnSurface = Color(0xFF000000)
val LightSecondary = Color(0xFF8E8E93)

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
    onSecondary = Color.White,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkSecondary
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
    onSecondary = Color.White,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightSecondary
)

@Composable
fun AppleMusicOfflineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Segue o tema do sistema
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
