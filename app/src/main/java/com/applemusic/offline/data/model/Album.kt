package com.applemusic.offline.data.model

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val albumArtUri: String?,
    val songCount: Int,
    val year: Int?,
    val songs: List<Song>
)
