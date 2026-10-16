package com.applemusic.offline.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.applemusic.offline.MainActivity
import com.applemusic.offline.R
import com.applemusic.offline.data.model.Song
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@AndroidEntryPoint
class MediaPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    // Estado do player exposto como StateFlow
    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        createNotificationChannel()
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            mediaSession = MediaSession.Builder(this, exoPlayer).build()

            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_IDLE -> _playbackState.value = PlaybackState.Idle
                        Player.STATE_BUFFERING -> _playbackState.value = PlaybackState.Buffering
                        Player.STATE_READY -> {
                            _duration.value = exoPlayer.duration
                            if (exoPlayer.isPlaying) {
                                _playbackState.value = PlaybackState.Playing
                            } else {
                                _playbackState.value = PlaybackState.Paused
                            }
                        }
                        Player.STATE_ENDED -> _playbackState.value = PlaybackState.Ended
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playbackState.value = if (isPlaying) {
                        PlaybackState.Playing
                    } else {
                        PlaybackState.Paused
                    }
                }
            })
        }

        // Atualizar posição periodicamente
        serviceScope.launch {
            while (true) {
                player?.let {
                    if (it.isPlaying) {
                        _currentPosition.value = it.currentPosition
                    }
                }
                delay(1000)
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Music Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Music playback controls"
        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        player?.let { exoPlayer ->
            exoPlayer.setMediaItems(songs.map { MediaItem.fromUri(it.uri) })
            exoPlayer.prepare()
            exoPlayer.seekTo(startIndex, 0)
            exoPlayer.play()
            _currentSong.value = songs.getOrNull(startIndex)
        }
    }

    fun playPause() {
        player?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    fun skipToNext() {
        player?.seekToNext()
    }

    fun skipToPrevious() {
        player?.seekToPrevious()
    }

    fun seekTo(position: Long) {
        player?.seekTo(position)
    }

    fun setShuffleMode(enabled: Boolean) {
        player?.shuffleModeEnabled = enabled
    }

    fun setRepeatMode(repeatMode: Int) {
        player?.repeatMode = repeatMode
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "music_playback_channel"
        private const val NOTIFICATION_ID = 1
    }
}

// Estados do player (baseados no documento de análise)
sealed class PlaybackState {
    object Idle : PlaybackState()
    object Playing : PlaybackState()
    object Paused : PlaybackState()
    object Buffering : PlaybackState()
    object Ended : PlaybackState()
    data class Error(val message: String) : PlaybackState()
}
