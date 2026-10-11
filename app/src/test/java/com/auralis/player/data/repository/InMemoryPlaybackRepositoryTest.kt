package com.auralis.player.data.repository

import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.QueueItem
import com.auralis.player.domain.model.RepeatMode
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.model.SongId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryPlaybackRepositoryTest {

    private val repository = InMemoryPlaybackRepository()

    private fun song(id: String, durationMs: Long = 100_000L) = Song(
        id = SongId(id),
        sourceUri = "uri://$id",
        title = id,
        durationMs = durationMs,
    )

    private fun queue(vararg ids: String) = ids.map { QueueItem(uid = it, song = song(it)) }

    private fun loadQueue() {
        repository.setQueue(queue("a", "b", "c"), startIndex = 0)
    }

    @Test
    fun `estado inicial es idle`() = runTest {
        assertEquals(PlaybackState.IDLE, repository.getCurrentState())
        assertNull(repository.getCurrentState().currentSong)
        assertFalse(repository.getCurrentState().isPlaying)
    }

    @Test
    fun `play sin cola no activa reproduccion`() = runTest {
        repository.play()

        assertFalse(repository.getCurrentState().isPlaying)
    }

    @Test
    fun `setQueue posiciona la cancion inicial`() = runTest {
        loadQueue()

        val state = repository.getCurrentState()
        assertEquals("a", state.currentSong?.id?.value)
        assertEquals(song("a").durationMs, state.durationMs)
        assertFalse(state.isPlaying)
    }

    @Test
    fun `play y pause actualizan isPlaying`() = runTest {
        loadQueue()

        repository.play()
        assertTrue(repository.getCurrentState().isPlaying)

        repository.pause()
        assertFalse(repository.getCurrentState().isPlaying)
    }

    @Test
    fun `skipNext avanza y skipPrevious reinicia si hay progreso`() = runTest {
        loadQueue()

        repository.play()
        repository.skipNext()
        assertEquals("b", repository.getCurrentState().currentSong?.id?.value)

        repository.seekTo(5_000L)
        repository.skipPrevious()
        assertEquals("b", repository.getCurrentState().currentSong?.id?.value)
        assertEquals(0L, repository.getCurrentState().positionMs)

        repository.skipPrevious()
        assertEquals("a", repository.getCurrentState().currentSong?.id?.value)
    }

    @Test
    fun `skip en el limite se detiene sin repeat ALL`() = runTest {
        loadQueue()

        repository.skipPrevious()
        assertEquals("a", repository.getCurrentState().currentSong?.id?.value)

        repository.skipNext()
        repository.skipNext()
        repository.skipNext()
        assertEquals("c", repository.getCurrentState().currentSong?.id?.value)
    }

    @Test
    fun `repeat ALL hace circular la cola`() = runTest {
        loadQueue()
        repository.setRepeatMode(RepeatMode.ALL)

        repository.skipPrevious()
        assertEquals("c", repository.getCurrentState().currentSong?.id?.value)

        repository.skipNext()
        assertEquals("a", repository.getCurrentState().currentSong?.id?.value)
    }

    @Test
    fun `seekTo limita a los bounds de duracion`() = runTest {
        loadQueue()
        repository.play()

        repository.seekTo(-10L)
        assertEquals(0L, repository.getCurrentState().positionMs)

        repository.seekTo(500_000L)
        assertEquals(100_000L, repository.getCurrentState().positionMs)
    }

    @Test
    fun `shuffle y repeat se reflejan en el estado`() = runTest {
        repository.setShuffleEnabled(true)
        repository.setRepeatMode(RepeatMode.ONE)

        assertTrue(repository.getCurrentState().shuffleEnabled)
        assertEquals(RepeatMode.ONE, repository.observePlaybackState().first().repeatMode)
    }

    @Test
    fun `reemplazar la cola cambia la cancion actual`() = runTest {
        loadQueue()

        repository.setQueue(queue("x", "y"), startIndex = 1)

        assertEquals("y", repository.getCurrentState().currentSong?.id?.value)
    }

    @Test
    fun `addSongToQueue sobre cola vacia no falla y arranca la reproduccion`() = runTest {
        repository.addSongToQueue(song("only"))

        assertEquals("only", repository.getCurrentState().currentSong?.id?.value)
    }

    @Test
    fun `addSongToQueue inserta despues de la actual tras saltar`() = runTest {
        loadQueue() // a, b, c. Current: a
        repository.skipNext() // current: b, lastQueued sigue en a

        repository.addSongToQueue(song("d")) // a, b, d, c

        assertEquals("b", repository.getCurrentState().currentSong?.id?.value)
        repository.skipNext()
        assertEquals("d", repository.getCurrentState().currentSong?.id?.value)
    }

    @Test
    fun `addSongToQueue agrega a continuacion sin cambiar la actual`() = runTest {
        loadQueue() // a, b, c. Current: a
        val extra = song("d")

        repository.addSongToQueue(extra) // a, d, b, c

        assertEquals("a", repository.getCurrentState().currentSong?.id?.value)
        
        repository.skipNext()
        assertEquals("d", repository.getCurrentState().currentSong?.id?.value)
        
        repository.skipNext()
        assertEquals("b", repository.getCurrentState().currentSong?.id?.value)
    }

    @Test
    fun `observeQueue refleja la cola y sus modificaciones`() = runTest {
        loadQueue()

        assertEquals(listOf("a", "b", "c"), repository.observeQueue().first().map { it.id.value })

        repository.addSongToQueue(song("d"))
        assertEquals(listOf("a", "d", "b", "c"), repository.observeQueue().first().map { it.id.value })

        repository.moveQueueItem(fromIndex = 3, toIndex = 1)
        assertEquals(listOf("a", "c", "d", "b"), repository.observeQueue().first().map { it.id.value })

        repository.removeQueueItem(index = 0)
        assertEquals(listOf("c", "d", "b"), repository.observeQueue().first().map { it.id.value })
    }

    @Test
    fun `moveQueueItem mantiene la cancion actual acorde al reorden`() = runTest {
        loadQueue()
        repository.play()
        repository.skipNext() // current: b (index 1)

        repository.moveQueueItem(fromIndex = 2, toIndex = 0) // [c, a, b], current sigue en "b" (index 2)

        assertEquals("b", repository.getCurrentState().currentSong?.id?.value)
        repository.skipPrevious()
        assertEquals("a", repository.getCurrentState().currentSong?.id?.value)
    }

    @Test
    fun `removeQueueItem de la cancion actual pasa a la siguiente`() = runTest {
        loadQueue()
        repository.play()
        repository.skipNext() // current: b (index 1)

        repository.removeQueueItem(index = 1) // a, c

        assertEquals("c", repository.getCurrentState().currentSong?.id?.value)
        assertTrue(repository.getCurrentState().isPlaying)
    }
}
