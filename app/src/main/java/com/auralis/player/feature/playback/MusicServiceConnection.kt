package com.auralis.player.feature.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.RepeatMode
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.model.SongId
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicServiceConnection @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    val mediaController: MediaController?
        get() = if (mediaControllerFuture?.isDone == true) mediaControllerFuture?.get() else null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        mediaControllerFuture?.addListener({
            val controller = mediaControllerFuture?.get()
            controller?.addListener(PlayerListener())
            updatePlaybackState()
        }, MoreExecutors.directExecutor())

        // Periodically update position if playing
        scope.launch {
            while (true) {
                if (_playbackState.value.isPlaying) {
                    updatePlaybackState()
                }
                delay(1000)
            }
        }
    }

    private fun updatePlaybackState() {
        val controller = mediaController ?: return
        
        val currentMediaItem = controller.currentMediaItem
        val currentSong = currentMediaItem?.toSong()

        _playbackState.update {
            it.copy(
                currentSong = currentSong,
                isPlaying = controller.isPlaying,
                positionMs = controller.currentPosition,
                durationMs = if (controller.duration > 0) controller.duration else null,
                shuffleEnabled = controller.shuffleModeEnabled,
                repeatMode = when (controller.repeatMode) {
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
            )
        }
    }

    private inner class PlayerListener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            updatePlaybackState()
        }
    }

    private fun MediaItem.toSong(): Song? {
        val id = mediaId
        if (id.isEmpty()) return null
        
        return Song(
            id = SongId(id),
            sourceUri = requestMetadata.mediaUri?.toString() ?: "",
            title = mediaMetadata.title?.toString() ?: "Unknown",
            artist = mediaMetadata.artist?.toString(),
            durationMs = mediaMetadata.extras?.getLong("duration_ms")
        )
    }
}
