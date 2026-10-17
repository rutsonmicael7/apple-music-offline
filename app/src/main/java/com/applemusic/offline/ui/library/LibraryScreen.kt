package com.applemusic.offline.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.applemusic.offline.data.model.Song
import com.applemusic.offline.ui.theme.AppleRed
import com.applemusic.offline.viewmodel.LibraryViewModel

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    onSongClick: (Song, List<Song>, Int) -> Unit
) {
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Cores adaptativas
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color(0xFF000000) else Color(0xFFFFFFFF)
    val surfaceColor = if (isDarkTheme) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
    val textColor = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF000000)
    val secondaryTextColor = Color(0xFF8E8E93)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Cabeçalho
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Biblioteca",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 34.sp
            )

            IconButton(onClick = { viewModel.refreshLibrary() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Atualizar",
                    tint = AppleRed
                )
            }
        }

        // Tabs
        var selectedTab by remember { mutableStateOf(0) }
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = backgroundColor,
            contentColor = AppleRed, // Cor de seleção é o vermelho Apple
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = AppleRed // Indicador vermelho
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { 
                    Text(
                        "Músicas",
                        fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedTab == 0) AppleRed else secondaryTextColor // Selecionado = vermelho
                    ) 
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { 
                    Text(
                        "Álbuns",
                        fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedTab == 1) AppleRed else secondaryTextColor
                    ) 
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { 
                    Text(
                        "Artistas",
                        fontWeight = if (selectedTab == 2) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedTab == 2) AppleRed else secondaryTextColor
                    ) 
                }
            )
        }

        // Conteúdo
        when (selectedTab) {
            0 -> SongsListView(
                songs = songs,
                isLoading = isLoading,
                onSongClick = onSongClick,
                backgroundColor = backgroundColor,
                surfaceColor = surfaceColor,
                textColor = textColor,
                secondaryTextColor = secondaryTextColor
            )
            1 -> AlbumsView(songs = songs, surfaceColor = surfaceColor, textColor = textColor, secondaryTextColor = secondaryTextColor)
            2 -> ArtistsView(songs = songs, surfaceColor = surfaceColor, textColor = textColor, secondaryTextColor = secondaryTextColor)
        }
    }
}

@Composable
fun SongsListView(
    songs: List<Song>,
    isLoading: Boolean,
    onSongClick: (Song, List<Song>, Int) -> Unit,
    backgroundColor: Color,
    surfaceColor: Color,
    textColor: Color,
    secondaryTextColor: Color
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AppleRed)
        }
    } else if (songs.isEmpty()) {
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
                    tint = secondaryTextColor,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nenhuma música encontrada",
                    style = MaterialTheme.typography.titleLarge,
                    color = secondaryTextColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Toque em atualizar para escanear",
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryTextColor
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(songs) { song ->
                SongListItem(
                    song = song,
                    onClick = { onSongClick(song, songs, songs.indexOf(song)) },
                    surfaceColor = surfaceColor,
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor
                )
            }
        }
    }
}

@Composable
fun SongListItem(
    song: Song,
    onClick: () -> Unit,
    surfaceColor: Color,
    textColor: Color,
    secondaryTextColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(surfaceColor),
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
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = secondaryTextColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryTextColor,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = song.formattedDuration,
            style = MaterialTheme.typography.bodySmall,
            color = secondaryTextColor,
            fontSize = 12.sp
        )
    }
}

@Composable
fun AlbumsView(songs: List<Song>, surfaceColor: Color, textColor: Color, secondaryTextColor: Color) {
    val albums = songs.groupBy { it.album }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(albums.entries.toList()) { entry ->
            val albumName = entry.key
            val albumSongs = entry.value

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(surfaceColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Album,
                        contentDescription = null,
                        tint = secondaryTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = albumName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${albumSongs.size} músicas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryTextColor,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ArtistsView(songs: List<Song>, surfaceColor: Color, textColor: Color, secondaryTextColor: Color) {
    val artists = songs.groupBy { it.artist }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(artists.entries.toList()) { entry ->
            val artistName = entry.key
            val artistSongs = entry.value

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(surfaceColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = secondaryTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = artistName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${artistSongs.size} músicas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryTextColor,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
