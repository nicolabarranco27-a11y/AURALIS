// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.feature.player

import androidx.compose.ui.graphics.Color
import com.auralis.player.core.ui.theme.AuraFallbackColor
import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.QueueItem
import com.auralis.player.domain.model.RepeatMode
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.model.SongId
import com.auralis.player.domain.repository.ArtworkColorRepository
import com.auralis.player.domain.repository.PlaybackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sleep timer pausa la reproduccion al agotarse`() = runTest(dispatcher) {
        val repository = FakePlaybackRepository()
        val viewModel = PlayerViewModel(repository, FakeArtworkColorRepository())

        viewModel.startSleepTimer(5_000L)
        runCurrent()
        assertTrue(viewModel.sleepTimer.value is SleepTimerState.Running)

        advanceTimeBy(5_001L)
        runCurrent()

        assertTrue(repository.pauseCalled)
        assertEquals(SleepTimerState.Off, viewModel.sleepTimer.value)
    }

    @Test
    fun `cancelar el sleep timer evita la pausa`() = runTest(dispatcher) {
        val repository = FakePlaybackRepository()
        val viewModel = PlayerViewModel(repository, FakeArtworkColorRepository())

        viewModel.startSleepTimer(10_000L)
        runCurrent()
        viewModel.cancelSleepTimer()

        advanceTimeBy(20_000L)
        runCurrent()

        assertFalse(repository.pauseCalled)
        assertEquals(SleepTimerState.Off, viewModel.sleepTimer.value)
    }

    @Test
    fun `toggle play pause delega en el repositorio`() = runTest(dispatcher) {
        val repository = FakePlaybackRepository()
        val viewModel = PlayerViewModel(repository, FakeArtworkColorRepository())

        viewModel.togglePlayPause()

        assertTrue(repository.playCalled)
    }

    @Test
    fun `el aura usa el color de respaldo cuando no hay caratula`() = runTest(dispatcher) {
        val repository = FakePlaybackRepository()
        val viewModel = PlayerViewModel(repository, FakeArtworkColorRepository())

        runCurrent()

        assertEquals(AuraFallbackColor, viewModel.artworkAura.value)
    }

    @Test
    fun `el aura toma el color dominante de la caratula`() = runTest(dispatcher) {
        val repository = FakePlaybackRepository()
        val colorRepository = FakeArtworkColorRepository(color = 0xFF3355AA.toInt())
        val viewModel = PlayerViewModel(repository, colorRepository)

        repository.emit(
            PlaybackState(currentSong = song(coverReference = "content://art/1"))
        )
        runCurrent()

        assertEquals(Color(0xFF3355AA.toInt()), viewModel.artworkAura.value)
    }

    private fun song(coverReference: String?): Song = Song(
        id = SongId("1"),
        sourceUri = "content://song/1",
        title = "Cancion",
        coverReference = coverReference,
    )

    private class FakeArtworkColorRepository(
        private val color: Int? = null,
    ) : ArtworkColorRepository {
        override suspend fun dominantColorFor(artworkReference: String?): Int? =
            if (artworkReference == null) null else color
    }

    private class FakePlaybackRepository : PlaybackRepository {
        private val _state = MutableStateFlow(PlaybackState.IDLE)
        var playCalled = false
        var pauseCalled = false

        fun emit(state: PlaybackState) {
            _state.value = state
        }

        override fun observePlaybackState(): Flow<PlaybackState> = _state.asStateFlow()
        override suspend fun getCurrentState(): PlaybackState = _state.value
        override fun play() { playCalled = true; _state.value = _state.value.copy(isPlaying = true) }
        override fun pause() { pauseCalled = true; _state.value = _state.value.copy(isPlaying = false) }
        override fun seekTo(positionMs: Long) {}
        override fun skipNext() {}
        override fun skipPrevious() {}
        override fun setShuffleEnabled(enabled: Boolean) {}
        override fun setRepeatMode(mode: RepeatMode) {}
        override fun setQueue(items: List<QueueItem>, startIndex: Int) {}
        override fun addSongToQueue(song: Song) {}
        override fun observeQueue(): Flow<List<Song>> = MutableStateFlow<List<Song>>(emptyList()).asStateFlow()
        override fun moveQueueItem(fromIndex: Int, toIndex: Int) {}
        override fun removeQueueItem(index: Int) {}
        override fun applyEqualizerBands(bands: List<Float>, preset: String) {}
    }
}
