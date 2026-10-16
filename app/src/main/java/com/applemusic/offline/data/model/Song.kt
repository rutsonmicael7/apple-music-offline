package com.applemusic.offline.data.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey
    val id: String,           // ID único (baseado no caminho do arquivo)
    val title: String,        // Título da música
    val artist: String,       // Artista
    val album: String,        // Álbum
    val duration: Long,       // Duração em milissegundos
    val path: String,         // Caminho do arquivo
    val albumArtUri: String?, // URI da capa do álbum
    val trackNumber: Int,     // Número da faixa
    val year: Int,            // Ano
    val genre: String?,       // Gênero
    val dateAdded: Long,      // Data de adição à biblioteca
    val lastPlayed: Long?,    // Última vez tocada
    val playCount: Int        // Contagem de reproduções
) {
    // URI para reprodução
    val uri: Uri
        get() = Uri.parse("file://$path")

    // Duração formatada (mm:ss)
    val formattedDuration: String
        get() {
            val minutes = duration / 1000 / 60
            val seconds = duration / 1000 % 60
            return "%d:%02d".format(minutes, seconds)
        }
}
