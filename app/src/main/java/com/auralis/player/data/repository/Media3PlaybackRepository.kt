package com.auralis.player.data.repository

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.QueueItem
import com.auralis.player.domain.model.RepeatMode
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.repository.PlaybackRepository
import com.auralis.player.feature.playback.MusicServiceConnection
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Media3PlaybackRepository @Inject constructor(
    private val serviceConnection: MusicServiceConnection
) : PlaybackRepository {

    override fun observePlaybackState(): Flow<PlaybackState> = serviceConnection.playbackState

    override suspend fun getCurrentState(): PlaybackState = serviceConnection.playbackState.value

    override fun play() {
        serviceConnection.mediaController?.play()
    }

    override fun pause() {
        serviceConnection.mediaController?.pause()
    }

    override fun seekTo(positionMs: Long) {
        serviceConnection.mediaController?.seekTo(positionMs)
    }

    override fun skipNext() {
        serviceConnection.mediaController?.seekToNext()
    }

    override fun skipPrevious() {
        serviceConnection.mediaController?.seekToPrevious()
    }

    override fun setShuffleEnabled(enabled: Boolean) {
        serviceConnection.mediaController?.shuffleModeEnabled = enabled
    }

    override fun setRepeatMode(mode: RepeatMode) {
        serviceConnection.mediaController?.repeatMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
    }

    override fun setQueue(items: List<QueueItem>, startIndex: Int) {
        val controller = serviceConnection.mediaController ?: return
        val mediaItems = items.map { it.song.toMediaItem() }
        controller.setMediaItems(mediaItems, startIndex, 0L)
        controller.prepare()
        controller.play()
    }

    private fun Song.toMediaItem(): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setExtras(Bundle().apply {
                durationMs?.let { putLong("duration_ms", it) }
            })
            .build()

        return MediaItem.Builder()
            .setMediaId(id.value)
            .setUri(Uri.parse(sourceUri))
            .setMediaMetadata(metadata)
            .build()
    }
}
