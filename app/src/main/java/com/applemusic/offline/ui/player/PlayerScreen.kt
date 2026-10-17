package com.applemusic.offline.ui.player

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.applemusic.offline.data.model.Song
import com.applemusic.offline.ui.theme.ApplePink
import com.applemusic.offline.ui.theme.AppleRed
import com.applemusic.offline.viewmodel.PlayerViewModel
import com.applemusic.offline.viewmodel.PlaybackState
import com.applemusic.offline.viewmodel.RepeatMode
import kotlinx.coroutines.launch

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()

    val primaryTextColor = Color.White
    val secondaryTextColor = Color(0xB3FFFFFF)
    val backgroundColor = Color(0xFF1a1a1a)

    // Estados
    var backgroundBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var targetColor by remember { mutableStateOf(Color(0xFF1a1a1a)) }

    // Animação 1000ms
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "colorTransition"
    )

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // SISTEMA REAL DA APPLE: extrair cor quando música muda
    LaunchedEffect(currentSong?.albumArtUri) {
        currentSong?.albumArtUri?.let { uri ->
            scope.launch {
                try {
                    val loader = ImageLoader(context)
                    val request = ImageRequest.Builder(context)
                        .data(uri)
                        .allowHardware(false)
                        .build()

                    val result = loader.execute(request)
                    if (result is SuccessResult) {
                        val drawable = result.drawable
                        if (drawable is BitmapDrawable) {
                            val bitmap = drawable.bitmap

                            // 1. Extrair cor (método Apple real)
                            val extractedColor = extractAppleColor(bitmap)
                            targetColor = Color(extractedColor)

                            // 2. Criar background com blur pesado (método Apple)
                            backgroundBitmap = createBlurredBackground(bitmap, extractedColor)
                        }
                    }
                } catch (e: Exception) { }
            }
        }
    }

    BackHandler { onNavigateBack() }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        // LAYER 1: Background com blur pesado + overlay de cor
        backgroundBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // LAYER 2: Gradiente radial com cor animada
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(
                        animatedColor.copy(alpha = 0.3f),
                        animatedColor.copy(alpha = 0.15f),
                        backgroundColor
                    ),
                    center = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                    radius = 1500f
                )
            )
        )

        // LAYER 3: Scrim variável
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.15f),
                        Color.Black.copy(alpha = 0.25f),
                        Color.Black.copy(alpha = 0.35f)
                    ),
                    radius = 1200f
                )
            )
        )

        // LAYER 4: Gradientes de legibilidade
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp).background(
                Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent))
            )
        )
        Box(
            modifier = Modifier.fillMaxWidth().height(300.dp).align(Alignment.BottomCenter).background(
                Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f)))
            )
        )

        // CONTEÚDO
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.KeyboardArrowDown, "Voltar", tint = primaryTextColor, modifier = Modifier.size(32.dp))
                }
                Text("Tocando Agora", color = secondaryTextColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            currentSong?.let { song ->
                val elevation = if (playbackState is PlaybackState.Playing) 8.dp else 4.dp
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                        .shadow(elevation, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1C1C1E)),
                    contentAlignment = Alignment.Center
                ) {
                    if (song.albumArtUri != null) {
                        AsyncImage(model = song.albumArtUri, contentDescription = "Album art", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(AppleRed, ApplePink))), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MusicNote, null, tint = Color.White, modifier = Modifier.size(80.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(song.title, color = primaryTextColor, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Text(song.artist, color = secondaryTextColor, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(24.dp))

                Column {
                    Slider(
                        value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                        onValueChange = { viewModel.seekTo((it * duration).toLong()) },
                        colors = SliderDefaults.colors(thumbColor = primaryTextColor, activeTrackColor = primaryTextColor, inactiveTrackColor = primaryTextColor.copy(alpha = 0.3f))
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(viewModel.formatTime(currentPosition), color = secondaryTextColor, fontSize = 12.sp)
                        Text(viewModel.formatTime(duration), color = secondaryTextColor, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.toggleShuffle() }) {
                        Icon(Icons.Default.Shuffle, "Shuffle", tint = if (isShuffleEnabled) AppleRed else secondaryTextColor, modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = { viewModel.skipToPrevious() }) {
                        Icon(Icons.Default.SkipPrevious, "Previous", tint = primaryTextColor, modifier = Modifier.size(32.dp))
                    }
                    FloatingActionButton(
                        onClick = { viewModel.playPause() },
                        modifier = Modifier.size(72.dp).shadow(8.dp, CircleShape),
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        shape = CircleShape
                    ) {
                        Icon(if (playbackState is PlaybackState.Playing) Icons.Default.Pause else Icons.Default.PlayArrow, "Play/Pause", modifier = Modifier.size(48.dp))
                    }
                    IconButton(onClick = { viewModel.skipToNext() }) {
                        Icon(Icons.Default.SkipNext, "Next", tint = primaryTextColor, modifier = Modifier.size(32.dp))
                    }
                    IconButton(onClick = { viewModel.cycleRepeatMode() }) {
                        Icon(
                            when (repeatMode) { RepeatMode.OFF -> Icons.Default.Repeat; RepeatMode.ALL -> Icons.Default.Repeat; RepeatMode.ONE -> Icons.Default.RepeatOne },
                            "Repeat",
                            tint = if (repeatMode == RepeatMode.OFF) secondaryTextColor else AppleRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// SISTEMA REAL DA APPLE: Extrair cor
private fun extractAppleColor(bitmap: Bitmap): Int {
    // 1. REDUZ PELA METADE (como a Apple faz)
    val reduced = Bitmap.createScaledBitmap(
        bitmap,
        bitmap.width / 2,
        bitmap.height / 2,
        true  // filter = true (cria blur suave)
    )

    // 2. Extrai cor média do bitmap reduzido
    val avgColor = calculateAverageColor(reduced)

    // 3. Ajusta saturação +30%, brilho -10%
    return adjustColor(avgColor)
}

private fun calculateAverageColor(bitmap: Bitmap): Int {
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)

    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    var r = 0L
    var g = 0L  
    var b = 0L

    for (pixel in pixels) {
        r += AndroidColor.red(pixel)
        g += AndroidColor.green(pixel)
        b += AndroidColor.blue(pixel)
    }

    val count = pixels.size
    return AndroidColor.rgb(
        (r / count).toInt(),
        (g / count).toInt(), 
        (b / count).toInt()
    )
}

private fun adjustColor(color: Int): Int {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(color, hsv)

    // Saturação +30%
    hsv[1] = (hsv[1] * 1.3f).coerceIn(0f, 1f)

    // Brilho -10%
    hsv[2] = (hsv[2] * 0.9f).coerceIn(0f, 1f)

    return AndroidColor.HSVToColor(hsv)
}

// Criar background com blur pesado (método Apple)
private fun createBlurredBackground(bitmap: Bitmap, color: Int): Bitmap {
    // Reduz MUITO para blur pesado (25x25)
    val tiny = Bitmap.createScaledBitmap(bitmap, 25, 25, true)

    // Escala de volta com filtro (cria blur)
    val blurred = Bitmap.createScaledBitmap(tiny, 1000, 1000, true)

    // Cria bitmap final com overlay de cor
    val result = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(result)

    // Desenha blur
    canvas.drawBitmap(blurred, 0f, 0f, null)

    // Overlay com cor vibrante (alpha 40%)
    val colorPaint = android.graphics.Paint().apply {
        this.color = color
        alpha = 100  // ~40%
    }
    canvas.drawRect(0f, 0f, 1000f, 1000f, colorPaint)

    return result
}
