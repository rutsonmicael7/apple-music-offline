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
import kotlin.math.abs
import kotlin.math.max

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
    val secondaryTextColor = Color.White.copy(alpha = 0.7f)
    val backgroundColor = Color(0xFF1a1a1a)

    // Estados
    var blurredBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var targetColor by remember { mutableStateOf(Color(0xFF1a1a1a)) }

    // Animação de cor - 1000ms com curva ease in out
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "colorTransition"
    )

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Extrair cor estilo Apple quando música muda
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
                            blurredBitmap = bitmap

                            // Extrair cor com ajustes Apple
                            val appleColor = extractAppleStyleColor(bitmap)
                            targetColor = Color(appleColor)
                        }
                    }
                } catch (e: Exception) {
                    // Ignora
                }
            }
        }
    }

    BackHandler {
        onNavigateBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // CAMADA 1: Gradiente radial com 3 paradas (0f, 0.4f, 1f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            animatedColor,
                            animatedColor, // Mesma cor até 40% (zona clara no centro)
                            darkenColor(animatedColor, 0.5f) // Escurece nas bordas
                        ),
                        center = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                        radius = 1500f
                    )
                )
        )

        // CAMADA 2: Blur da capa (sigma 50, alpha 0.4)
        blurredBitmap?.let { bitmap ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(radius = 50.dp)
                        .alpha(0.4f),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // CAMADA 3: Scrim variável (centro 0.15, bordas 0.35)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.15f), // Centro mais claro
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.35f)  // Bordas mais escuras
                        ),
                        center = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                        radius = 1200f
                    )
                )
        )

        // CONTEÚDO
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Cabeçalho
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Voltar",
                        tint = primaryTextColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "Tocando Agora",
                    style = MaterialTheme.typography.labelMedium,
                    color = secondaryTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            currentSong?.let { song ->
                // Capa
                val elevation = if (playbackState is PlaybackState.Playing) 8.dp else 4.dp

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(
                            elevation = elevation,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = Color.Black.copy(alpha = 0.3f)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1C1C1E)),
                    contentAlignment = Alignment.Center
                ) {
                    if (song.albumArtUri != null) {
                        AsyncImage(
                            model = song.albumArtUri,
                            contentDescription = "Album art",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(AppleRed, ApplePink)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Título
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor,
                    fontSize = 22.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Artista
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = secondaryTextColor,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Progresso
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                        onValueChange = { fraction ->
                            viewModel.seekTo((fraction * duration).toLong())
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = primaryTextColor,
                            activeTrackColor = primaryTextColor,
                            inactiveTrackColor = primaryTextColor.copy(alpha = 0.3f)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = viewModel.formatTime(currentPosition),
                            style = MaterialTheme.typography.labelSmall,
                            color = secondaryTextColor,
                            fontSize = 12.sp
                        )
                        Text(
                            text = viewModel.formatTime(duration),
                            style = MaterialTheme.typography.labelSmall,
                            color = secondaryTextColor,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Controles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.toggleShuffle() }) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffleEnabled) AppleRed else secondaryTextColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(onClick = { viewModel.skipToPrevious() }) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = primaryTextColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    FloatingActionButton(
                        onClick = { viewModel.playPause() },
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(8.dp, CircleShape),
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = if (playbackState is PlaybackState.Playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playbackState is PlaybackState.Playing) "Pause" else "Play",
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(onClick = { viewModel.skipToNext() }) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = primaryTextColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(onClick = { viewModel.cycleRepeatMode() }) {
                        Icon(
                            imageVector = when (repeatMode) {
                                RepeatMode.OFF -> Icons.Default.Repeat
                                RepeatMode.ALL -> Icons.Default.Repeat
                                RepeatMode.ONE -> Icons.Default.RepeatOne
                            },
                            contentDescription = "Repeat",
                            tint = when (repeatMode) {
                                RepeatMode.OFF -> secondaryTextColor
                                else -> AppleRed
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma música tocando",
                        style = MaterialTheme.typography.titleLarge,
                        color = secondaryTextColor
                    )
                }
            }
        }
    }
}

// Extração de cor estilo Apple (com todos os ajustes)
private fun extractAppleStyleColor(bitmap: Bitmap): Int {
    // Reduz bitmap para 50x50
    val small = Bitmap.createScaledBitmap(bitmap, 50, 50, true)
    var r = 0f; var g = 0f; var b = 0f; var total = 0f
    val cx = small.width / 2; val cy = small.height / 2

    // Média ponderada pelo centro
    for (x in 0 until small.width step 2) {
        for (y in 0 until small.height step 2) {
            val pixel = small.getPixel(x, y)
            val weight = 1f - (abs(x - cx) + abs(y - cy)).toFloat() / (small.width + small.height)
            r += AndroidColor.red(pixel) * weight
            g += AndroidColor.green(pixel) * weight
            b += AndroidColor.blue(pixel) * weight
            total += weight
        }
    }

    val avg = AndroidColor.rgb((r/total).toInt(), (g/total).toInt(), (b/total).toInt())

    // Aplicar ajustes Apple: saturação 1.4, brilho 0.85, contraste 1.1
    return appleStyleColorAdjustment(avg)
}

private fun appleStyleColorAdjustment(color: Int): Int {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(color, hsv)

    // Saturação: aumenta 40%
    hsv[1] = (hsv[1] * 1.4f).coerceIn(0f, 1f)

    // Brilho: reduz 15%
    hsv[2] = (hsv[2] * 0.85f).coerceIn(0f, 1f)

    var adjusted = AndroidColor.HSVToColor(hsv)

    // Contraste sutil: 1.1
    adjusted = applyContrastCurve(adjusted, contrast = 1.1f)

    return adjusted
}

private fun applyContrastCurve(color: Int, contrast: Float): Int {
    val r = AndroidColor.red(color)
    val g = AndroidColor.green(color)
    val b = AndroidColor.blue(color)

    fun adjust(channel: Int): Int {
        val normalized = channel / 255f
        val adjusted = ((normalized - 0.5f) * contrast + 0.5f).coerceIn(0f, 1f)
        return (adjusted * 255).toInt()
    }

    return AndroidColor.rgb(adjust(r), adjust(g), adjust(b))
}

private fun darkenColor(color: Color, factor: Float): Color {
    return Color(
        red = color.red * factor,
        green = color.green * factor,
        blue = color.blue * factor,
        alpha = color.alpha
    )
}
