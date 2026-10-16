package com.applemusic.offline.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.applemusic.offline.data.MediaScanner
import com.applemusic.offline.data.database.SongDao
import com.applemusic.offline.data.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val songDao: SongDao,
    private val mediaScanner: MediaScanner
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        refreshLibrary()
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            _isLoading.value = true

            // Escanear device e salvar no banco
            val scannedSongs = mediaScanner.scanDevice()
            if (scannedSongs.isNotEmpty()) {
                songDao.insertSongs(scannedSongs)
            }

            // Carregar do banco
            songDao.getAllSongs().collect { songList ->
                _songs.value = songList
                _isLoading.value = false
            }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                songDao.getAllSongs().collect { _songs.value = it }
            } else {
                songDao.searchSongs(query).collect { _songs.value = it }
            }
        }
    }
}
