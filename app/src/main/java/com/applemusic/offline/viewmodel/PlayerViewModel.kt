package com.applemusic.offline.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.applemusic.offline.data.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor() : ViewModel() {

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

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        _queue.value = songs
        _currentQueueIndex.value = startIndex
        _currentSong.value = songs.getOrNull(startIndex)
        _playbackState.value = PlaybackState.Playing
        // TODO: Integrar com MediaPlaybackService
    }

    fun playPause() {
        _playbackState.value = when (_playbackState.value) {
            is PlaybackState.Playing -> PlaybackState.Paused
            else -> PlaybackState.Playing
        }
    }

    fun skipToNext() {
        val nextIndex = (_currentQueueIndex.value + 1) % _queue.value.size
        _currentQueueIndex.value = nextIndex
        _currentSong.value = _queue.value.getOrNull(nextIndex)
    }

    fun skipToPrevious() {
        val prevIndex = if (_currentQueueIndex.value > 0) {
            _currentQueueIndex.value - 1
        } else {
            _queue.value.size - 1
        }
        _currentQueueIndex.value = prevIndex
        _currentSong.value = _queue.value.getOrNull(prevIndex)
    }

    fun seekTo(position: Long) {
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
}

enum class RepeatMode {
    OFF, ALL, ONE
}
