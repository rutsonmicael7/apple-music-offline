package com.applemusic.offline.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.applemusic.offline.data.model.Song
import com.applemusic.offline.service.MediaPlaybackService
import com.applemusic.offline.service.PlaybackState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackService: MediaPlaybackService
) : ViewModel() {

    // Estado do player
    val playbackState: StateFlow<PlaybackState> = playbackService.playbackState
    val currentSong: StateFlow<Song?> = playbackService.currentSong
    val currentPosition: StateFlow<Long> = playbackService.currentPosition
    val duration: StateFlow<Long> = playbackService.duration

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
        playbackService.playSongs(songs, startIndex)
    }

    fun playPause() {
        playbackService.playPause()
    }

    fun skipToNext() {
        val nextIndex = (_currentQueueIndex.value + 1) % _queue.value.size
        _currentQueueIndex.value = nextIndex
        playbackService.skipToNext()
    }

    fun skipToPrevious() {
        val prevIndex = if (_currentQueueIndex.value > 0) {
            _currentQueueIndex.value - 1
        } else {
            _queue.value.size - 1
        }
        _currentQueueIndex.value = prevIndex
        playbackService.skipToPrevious()
    }

    fun seekTo(position: Long) {
        playbackService.seekTo(position)
    }

    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
        playbackService.setShuffleMode(_isShuffleEnabled.value)
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        playbackService.setRepeatMode(_repeatMode.value.toExoPlayerRepeatMode())
    }

    fun addToQueue(song: Song) {
        _queue.value = _queue.value + song
    }

    fun removeFromQueue(index: Int) {
        if (index in _queue.value.indices) {
            _queue.value = _queue.value.toMutableList().apply { removeAt(index) }
        }
    }

    fun moveInQueue(fromIndex: Int, toIndex: Int) {
        _queue.value = _queue.value.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
    }

    // Formatação de tempo
    fun formatTime(ms: Long): String {
        val minutes = ms / 1000 / 60
        val seconds = ms / 1000 % 60
        return "%d:%02d".format(minutes, seconds)
    }
}

enum class RepeatMode {
    OFF, ALL, ONE;

    fun toExoPlayerRepeatMode(): Int {
        return when (this) {
            OFF -> androidx.media3.common.Player.REPEAT_MODE_OFF
            ALL -> androidx.media3.common.Player.REPEAT_MODE_ALL
            ONE -> androidx.media3.common.Player.REPEAT_MODE_ONE
        }
    }
}
