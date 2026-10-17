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

    var blurredBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var targetColor by remember { mutableStateOf(Color(0xFF1a1a1a)) }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "colorTransition"
    )

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
                            val appleColor = extractAppleStyleColor(bitmap)
                            targetColor = Color(appleColor)
                        }
                    }
                } catch (e: Exception) { }
            }
        }
    }

    BackHandler { onNavigateBack() }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        // LAYER 1: Gradiente radial com cor vibrante
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(animatedColor, animatedColor, darkenColor(animatedColor, 0.5f)),
                    center = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                    radius = 1500f
                )
            )
        )

        // LAYER 2: Blur da capa com PorterDuff OVERLAY (efeito Apple!)
        blurredBitmap?.let { bitmap ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Técnica: blur + overlay color = efeito glass
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(50.dp)
                        .alpha(0.5f),
                    contentScale = ContentScale.Crop,
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                        animatedColor.copy(alpha = 0.3f),
                        blendMode = androidx.compose.ui.graphics.BlendMode.Overlay
                    )
                )
            }
        }

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

        // LAYER 4: Gradientes de legibilidade (LinearGradient horizontal como no código Apple!)
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

        // LAYER 5: Glass effect nas bordas (LinearGradient horizontal!)
        GlassEffectOverlay()

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

// Glass effect com LinearGradient horizontal (como no código Apple!)
@Composable
fun GlassEffectOverlay() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Topo: LinearGradient vertical
        Box(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.25f).background(
                Brush.verticalGradient(colors = listOf(Color.White.copy(alpha = 35f/255f), Color.Transparent))
            )
        )
        // Esquerda: LinearGradient horizontal!
        Box(
            modifier = Modifier.fillMaxHeight().fillMaxWidth(0.08f).background(
                Brush.horizontalGradient(colors = listOf(Color.White.copy(alpha = 20f/255f), Color.Transparent))
            )
        )
        // Direita: LinearGradient horizontal!
        Box(
            modifier = Modifier.fillMaxHeight().fillMaxWidth(0.08f).align(Alignment.CenterEnd).background(
                Brush.horizontalGradient(colors = listOf(Color.Transparent, Color.White.copy(alpha = 20f/255f)))
            )
        )
        // Fundo: LinearGradient vertical
        Box(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.15f).align(Alignment.BottomCenter).background(
                Brush.verticalGradient(colors = listOf(Color.Transparent, Color.White.copy(alpha = 15f/255f)))
            )
        )
    }
}

private fun extractAppleStyleColor(bitmap: Bitmap): Int {
    val small = Bitmap.createScaledBitmap(bitmap, 50, 50, true)
    var r = 0f; var g = 0f; var b = 0f; var total = 0f
    val cx = small.width / 2; val cy = small.height / 2
    for (x in 0 until small.width step 2) {
        for (y in 0 until small.height step 2) {
            val pixel = small.getPixel(x, y)
            val weight = 1f - (abs(x - cx) + abs(y - cy)).toFloat() / (small.width + small.height)
            r += AndroidColor.red(pixel) * weight; g += AndroidColor.green(pixel) * weight; b += AndroidColor.blue(pixel) * weight; total += weight
        }
    }
    return increaseSaturation(AndroidColor.rgb((r/total).toInt(), (g/total).toInt(), (b/total).toInt()), 1.4f)
}

private fun increaseSaturation(color: Int, factor: Float): Int {
    val hsv = FloatArray(3); AndroidColor.colorToHSV(color, hsv)
    hsv[1] = (hsv[1] * factor).coerceIn(0f, 1f); hsv[2] = (hsv[2] * 0.85f).coerceIn(0f, 1f)
    return AndroidColor.HSVToColor(hsv)
}

private fun darkenColor(color: Color, factor: Float): Color {
    return Color(color.red * factor, color.green * factor, color.blue * factor, color.alpha)
}
