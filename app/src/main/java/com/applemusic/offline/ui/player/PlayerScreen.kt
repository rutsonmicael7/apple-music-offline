package com.applemusic.offline.ui.player

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
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
    val backgroundColor = Color(0xFF1a1a1a) // Cor base do código Apple

    // Estados
    var blurredBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var currentColor by remember { mutableStateOf(Color(0xFF1a1a1a)) }
    var targetColor by remember { mutableStateOf(Color(0xFF1a1a1a)) }

    // Animação de transição de cor (800ms como no código Apple)
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
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

                            // Extrair cor estilo Apple
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
        // CAMADA 1: Background com gradiente radial (cor animada)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            animatedColor,
                            darkenColor(animatedColor, 0.5f),
                            backgroundColor
                        ),
                        center = androidx.compose.ui.geometry.Offset(0.5f, 0.4f),
                        radius = 1500f
                    )
                )
        )

        // CAMADA 2: Capa com blur (se disponível)
        blurredBitmap?.let { bitmap ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(radius = 80.dp)
                        .alpha(0.4f),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // CAMADA 3: Scrim preto 25%
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f))
        )

        // CAMADA 4: Gradiente topo (legibilidade título)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    )
                )
        )

        // CAMADA 5: Gradiente fundo (legibilidade controles)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.78f)
                        )
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

// Função para extrair cor estilo Apple (do código fornecido)
private fun extractAppleStyleColor(bitmap: Bitmap): Int {
    val small = Bitmap.createScaledBitmap(bitmap, 50, 50, true)
    var r = 0f; var g = 0f; var b = 0f; var total = 0f
    val cx = small.width / 2; val cy = small.height / 2

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
    return increaseSaturation(avg, 1.4f)
}

private fun increaseSaturation(color: Int, factor: Float): Int {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(color, hsv)
    hsv[1] = (hsv[1] * factor).coerceIn(0f, 1f)
    hsv[2] = (hsv[2] * 0.85f).coerceIn(0f, 1f)
    return AndroidColor.HSVToColor(hsv)
}

private fun darkenColor(color: Color, factor: Float): Color {
    return Color(
        red = color.red * factor,
        green = color.green * factor,
        blue = color.blue * factor,
        alpha = color.alpha
    )
}
