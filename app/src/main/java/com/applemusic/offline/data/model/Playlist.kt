package com.applemusic.offline.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String?,
    val coverArtUri: String?,
    val songCount: Int,
    val dateCreated: Long,
    val dateModified: Long
)
