package com.applemusic.offline.viewmodel

import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.applemusic.offline.data.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor() : ViewModel() {

    private var mediaPlayer: MediaPlayer? = null

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentQueueIndex = MutableStateFlow(0)
    val currentQueueIndex: StateFlow<Int> = _currentQueueIndex.asStateFlow()

    private var positionUpdateJob: Job? = null

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty() || startIndex >= songs.size) return

        _queue.value = songs
        _currentQueueIndex.value = startIndex
        playSong(songs[startIndex])
    }

    private fun playSong(song: Song) {
        // Parar player anterior
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e("PlayerViewModel", "Error releasing media player", e)
        }
        positionUpdateJob?.cancel()

        _currentSong.value = song
        _playbackState.value = PlaybackState.Buffering

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(song.path)
                prepare()
                start()

                _duration.value = this.duration.toLong()
                _playbackState.value = PlaybackState.Playing

                setOnCompletionListener {
                    when (_repeatMode.value) {
                        RepeatMode.ONE -> playSong(song)
                        else -> skipToNext()
                    }
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("PlayerViewModel", "MediaPlayer error: $what, $extra")
                    _playbackState.value = PlaybackState.Error("Erro de reprodução")
                    true
                }
            }

            startPositionUpdates()

        } catch (e: Exception) {
            Log.e("PlayerViewModel", "Error playing song", e)
            _playbackState.value = PlaybackState.Error(e.message ?: "Erro ao reproduzir")
        }
    }

    private fun startPositionUpdates() {
        positionUpdateJob = viewModelScope.launch {
            while (isActive) {
                try {
                    mediaPlayer?.let { player ->
                        if (player.isPlaying) {
                            _currentPosition.value = player.currentPosition.toLong()
                        }
                    }
                } catch (e: Exception) {
                    // Ignora erros de posição
                }
                delay(500)
            }
        }
    }

    fun playPause() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    _playbackState.value = PlaybackState.Paused
                } else {
                    player.start()
                    _playbackState.value = PlaybackState.Playing
                }
            }
        } catch (e: Exception) {
            Log.e("PlayerViewModel", "Error in playPause", e)
        }
    }

    fun skipToNext() {
        if (_queue.value.isEmpty()) return
        val nextIndex = (_currentQueueIndex.value + 1) % _queue.value.size
        _currentQueueIndex.value = nextIndex
        _queue.value.getOrNull(nextIndex)?.let { playSong(it) }
    }

    fun skipToPrevious() {
        if (_queue.value.isEmpty()) return
        val prevIndex = if (_currentQueueIndex.value > 0) {
            _currentQueueIndex.value - 1
        } else {
            _queue.value.size - 1
        }
        _currentQueueIndex.value = prevIndex
        _queue.value.getOrNull(prevIndex)?.let { playSong(it) }
    }

    fun seekTo(position: Long) {
        try {
            mediaPlayer?.seekTo(position.toInt())
            _currentPosition.value = position
        } catch (e: Exception) {
            Log.e("PlayerViewModel", "Error seeking", e)
        }
    }

    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun formatTime(ms: Long): String {
        val minutes = ms / 1000 / 60
        val seconds = ms / 1000 % 60
        return "%d:%02d".format(minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignora
        }
        positionUpdateJob?.cancel()
    }
}

sealed class PlaybackState {
    object Idle : PlaybackState()
    object Playing : PlaybackState()
    object Paused : PlaybackState()
    object Buffering : PlaybackState()
    object Ended : PlaybackState()
    data class Error(val message: String) : PlaybackState()
}

enum class RepeatMode {
    OFF, ALL, ONE
}
