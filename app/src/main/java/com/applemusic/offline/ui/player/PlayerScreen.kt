package com.applemusic.offline.ui.player

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.applemusic.offline.data.model.Song
import com.applemusic.offline.ui.theme.ApplePink
import com.applemusic.offline.ui.theme.AppleRed
import com.applemusic.offline.viewmodel.PlayerViewModel
import com.applemusic.offline.viewmodel.PlaybackState
import com.applemusic.offline.viewmodel.RepeatMode

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Cabeçalho minimalista
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tocando Agora",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF8E8E93),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            currentSong?.let { song ->
                // Capa do álbum - estilo Apple Music (grande, centralizada, cantos arredondados)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(
                            elevation = 8.dp,
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

                // Informações da música
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 22.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF8E8E93),
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Barra de progresso estilo Apple Music
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                        onValueChange = { fraction ->
                            viewModel.seekTo((fraction * duration).toLong())
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color(0xFF2C2C2E)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(currentPosition),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8E8E93),
                            fontSize = 12.sp
                        )
                        Text(
                            text = formatTime(duration),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8E8E93),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Controles principais - estilo Apple Music
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    IconButton(onClick = { viewModel.toggleShuffle() }) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffleEnabled) AppleRed else Color(0xFF8E8E93),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Previous
                    IconButton(onClick = { viewModel.skipToPrevious() }) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Play/Pause - botão grande central
                    FloatingActionButton(
                        onClick = { viewModel.playPause() },
                        modifier = Modifier.size(64.dp),
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

                    // Next
                    IconButton(onClick = { viewModel.skipToNext() }) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Repeat
                    IconButton(onClick = { viewModel.cycleRepeatMode() }) {
                        Icon(
                            imageVector = when (repeatMode) {
                                RepeatMode.OFF -> Icons.Default.Repeat
                                RepeatMode.ALL -> Icons.Default.Repeat
                                RepeatMode.ONE -> Icons.Default.RepeatOne
                            },
                            contentDescription = "Repeat",
                            tint = when (repeatMode) {
                                RepeatMode.OFF -> Color(0xFF8E8E93)
                                else -> AppleRed
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Controles secundários (volume, etc) - opcional
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.VolumeDown,
                            contentDescription = "Volume",
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Slider(
                        value = 0.7f,
                        onValueChange = { },
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF8E8E93),
                            activeTrackColor = Color(0xFF8E8E93),
                            inactiveTrackColor = Color(0xFF2C2C2E)
                        )
                    )

                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Volume",
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

            } ?: run {
                // Estado vazio
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhuma música tocando",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF8E8E93)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val minutes = ms / 1000 / 60
    val seconds = ms / 1000 % 60
    return "%d:%02d".format(minutes, seconds)
}
