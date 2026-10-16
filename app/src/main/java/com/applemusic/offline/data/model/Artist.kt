package com.applemusic.offline.data.model

data class Artist(
    val id: String,
    val name: String,
    val albumArtUri: String?,
    val albumCount: Int,
    val songCount: Int,
    val songs: List<Song>
)
