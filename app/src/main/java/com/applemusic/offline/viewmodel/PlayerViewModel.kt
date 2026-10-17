package com.applemusic.offline.viewmodel

import android.media.MediaPlayer
import android.net.Uri
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

    // Estado do player
    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    // Estado da UI
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentQueueIndex = MutableStateFlow(0)
    val currentQueueIndex: StateFlow<Int> = _currentQueueIndex.asStateFlow()

    // Job para atualizar posição
    private var positionUpdateJob: Job? = null

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        _queue.value = songs
        _currentQueueIndex.value = startIndex
        playSong(songs[startIndex])
    }

    private fun playSong(song: Song) {
        // Parar player anterior
        mediaPlayer?.release()
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

                // Listener quando termina
                setOnCompletionListener {
                    when (_repeatMode.value) {
                        RepeatMode.ONE -> playSong(song)
                        else -> skipToNext()
                    }
                }
            }

            // Atualizar posição periodicamente
            startPositionUpdates()

        } catch (e: Exception) {
            _playbackState.value = PlaybackState.Error(e.message ?: "Erro ao reproduzir")
        }
    }

    private fun startPositionUpdates() {
        positionUpdateJob = viewModelScope.launch {
            while (isActive) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPosition.value = player.currentPosition.toLong()
                    }
                }
                delay(500)
            }
        }
    }

    fun playPause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _playbackState.value = PlaybackState.Paused
            } else {
                player.start()
                _playbackState.value = PlaybackState.Playing
            }
        }
    }

    fun skipToNext() {
        val nextIndex = (_currentQueueIndex.value + 1) % _queue.value.size
        _currentQueueIndex.value = nextIndex
        _queue.value.getOrNull(nextIndex)?.let { playSong(it) }
    }

    fun skipToPrevious() {
        val prevIndex = if (_currentQueueIndex.value > 0) {
            _currentQueueIndex.value - 1
        } else {
            _queue.value.size - 1
        }
        _currentQueueIndex.value = prevIndex
        _queue.value.getOrNull(prevIndex)?.let { playSong(it) }
    }

    fun seekTo(position: Long) {
        mediaPlayer?.seekTo(position.toInt())
        _currentPosition.value = position
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
        mediaPlayer?.release()
        positionUpdateJob?.cancel()
    }
}

// Estados do player
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
