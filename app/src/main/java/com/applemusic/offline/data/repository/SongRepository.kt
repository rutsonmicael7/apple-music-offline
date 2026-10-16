package com.applemusic.offline.data.repository

import com.applemusic.offline.data.MediaScanner
import com.applemusic.offline.data.database.SongDao
import com.applemusic.offline.data.model.Song
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepository @Inject constructor(
    private val songDao: SongDao,
    private val mediaScanner: MediaScanner
) {
    fun getAllSongs(): Flow<List<Song>> = songDao.getAllSongs()

    fun getSongsByAlbum(album: String): Flow<List<Song>> = songDao.getSongsByAlbum(album)

    fun getSongsByArtist(artist: String): Flow<List<Song>> = songDao.getSongsByArtist(artist)

    fun getRecentlyPlayed(limit: Int = 50): Flow<List<Song>> = songDao.getRecentlyPlayed(limit)

    fun getMostPlayed(limit: Int = 50): Flow<List<Song>> = songDao.getMostPlayed(limit)

    fun searchSongs(query: String): Flow<List<Song>> = songDao.searchSongs(query)

    suspend fun getSongById(id: String): Song? = songDao.getSongById(id)

    suspend fun refreshLibrary() {
        val songs = mediaScanner.scanDevice()
        songDao.insertSongs(songs)
    }

    suspend fun updateLastPlayed(songId: String) {
        songDao.updateLastPlayed(songId, System.currentTimeMillis())
    }

    suspend fun deleteAllSongs() {
        songDao.deleteAllSongs()
    }

    suspend fun getSongCount(): Int = songDao.getSongCount()
}
