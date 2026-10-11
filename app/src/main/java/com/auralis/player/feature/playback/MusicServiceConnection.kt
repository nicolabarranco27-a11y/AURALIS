package com.auralis.player.feature.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
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

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null

    /**
     * Devuelve el MediaController una vez conectado, o null si aun no esta
     * listo o si la conexion fallo. El future puede completarse con
     * excepcion, por lo que get() se protege para no propagar ExecutionException.
     */
    val mediaController: MediaController?
        get() = try {
            if (mediaControllerFuture?.isDone == true) mediaControllerFuture?.get() else null
        } catch (_: Exception) {
            null
        }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        mediaControllerFuture?.addListener({
            try {
                val controller = mediaControllerFuture?.get()
                controller?.addListener(PlayerListener())
                updatePlaybackState()
                updateQueue()
            } catch (_: Exception) {
                // La conexion con el servicio fallo; se refleja como ausencia de reproductor.
            }
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
        val audioSessionId = controller.sessionExtras.getInt("audio_session_id", 0)

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
                },
                audioSessionId = audioSessionId
            )
        }
    }

    private fun updateQueue() {
        val controller = mediaController ?: return
        val songs = (0 until controller.mediaItemCount).mapNotNull { controller.getMediaItemAt(it).toSong() }
        if (songs != _queue.value) {
            _queue.value = songs
        }
    }

    private inner class PlayerListener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (player.playerError == null) {
                _playbackState.update { it.copy(errorMessage = null) }
            }
            updatePlaybackState()
            updateQueue()
        }

        override fun onPlayerError(error: PlaybackException) {
            _playbackState.update {
                it.copy(
                    isPlaying = false,
                    errorMessage = error.message ?: error.errorCodeName,
                )
            }
        }
    }

    private fun MediaItem.toSong(): Song? {
        val id = mediaId
        if (id.isEmpty()) return null

        val extras = mediaMetadata.extras
        return Song(
            id = SongId(id),
            sourceUri = requestMetadata.mediaUri?.toString() ?: "",
            title = mediaMetadata.title?.toString() ?: "Unknown",
            artist = mediaMetadata.artist?.toString(),
            album = mediaMetadata.albumTitle?.toString(),
            trackNumber = extras?.getInt("track_number", 0)?.takeIf { it > 0 },
            isFavorite = extras?.getBoolean("is_favorite", false) ?: false,
            durationMs = extras?.getLong("duration_ms"),
            coverReference = mediaMetadata.artworkUri?.toString()
        )
    }
}
