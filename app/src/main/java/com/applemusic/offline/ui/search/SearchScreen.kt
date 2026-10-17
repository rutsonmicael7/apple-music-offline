package com.applemusic.offline.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.applemusic.offline.data.model.Song
import com.applemusic.offline.ui.theme.AppleRed
import com.applemusic.offline.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onSongClick: (Song, List<Song>, Int) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()

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
        Text(
            text = "Buscar",
            style = MaterialTheme.typography.headlineLarge,
            color = textColor,
            fontSize = 34.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Campo de busca
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.onQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp)),
                placeholder = {
                    Text(
                        text = "Artistas, Músicas, Letras e mais",
                        color = secondaryTextColor
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = secondaryTextColor
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = secondaryTextColor
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = surfaceColor,
                    unfocusedContainerColor = surfaceColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = AppleRed,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
        }

        // Conteúdo
        if (searchQuery.isEmpty()) {
            // Estado vazio
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = secondaryTextColor,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Buscar na biblioteca",
                        style = MaterialTheme.typography.titleLarge,
                        color = secondaryTextColor
                    )
                }
            }
        } else {
            // Resultados
            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum resultado",
                        style = MaterialTheme.typography.titleLarge,
                        color = secondaryTextColor
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults.size) { index ->
                        val song = searchResults[index]
                        SearchResultItem(
                            song = song,
                            onClick = { onSongClick(song, searchResults, index) },
                            surfaceColor = surfaceColor,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
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
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
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
                    tint = secondaryTextColor
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${song.artist} • ${song.album}",
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
