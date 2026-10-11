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

    private var lastQueuedIndex: Int = -1

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
        lastQueuedIndex = startIndex
        controller.setMediaItems(mediaItems, startIndex, 0L)
        controller.prepare()
        controller.play()
    }

    override fun addSongToQueue(song: Song) {
        val controller = serviceConnection.mediaController ?: return
        val currentIndex = controller.currentMediaItemIndex

        // Si el índice guardado es inválido o menor al actual, empezamos a insertar después del actual.
        // Si es válido, insertamos después del último insertado para mantener el orden A -> B -> C.
        // Se acota al tamaño real de la cola para evitar IndexOutOfBoundsException cuando la
        // cola queda vacía o el servicio se recrea.
        val targetIndex = QueueInsertIndexCalculator.calculate(
            currentIndex = currentIndex,
            lastQueuedIndex = lastQueuedIndex,
            itemCount = controller.mediaItemCount,
        )

        controller.addMediaItem(targetIndex, song.toMediaItem())
        lastQueuedIndex = targetIndex
    }

    override fun observeQueue(): Flow<List<Song>> = serviceConnection.queue

    override fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val controller = serviceConnection.mediaController ?: return
        if (fromIndex !in 0 until controller.mediaItemCount) return
        val target = toIndex.coerceIn(0, controller.mediaItemCount - 1)
        if (fromIndex == target) return
        controller.moveMediaItem(fromIndex, target)
        lastQueuedIndex = -1
    }

    override fun removeQueueItem(index: Int) {
        val controller = serviceConnection.mediaController ?: return
        if (index !in 0 until controller.mediaItemCount) return
        controller.removeMediaItem(index)
        lastQueuedIndex = -1
    }

    private fun Song.toMediaItem(): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setArtworkUri(coverReference?.let { Uri.parse(it) })
            .setExtras(Bundle().apply {
                durationMs?.let { putLong("duration_ms", it) }
                trackNumber?.let { putInt("track_number", it) }
                putBoolean("is_favorite", isFavorite)
            })
            .build()

        return MediaItem.Builder()
            .setMediaId(id.value)
            .setUri(Uri.parse(sourceUri))
            .setMediaMetadata(metadata)
            .build()
    }
}
