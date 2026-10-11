package com.auralis.player.feature.player

import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.QueueItem
import com.auralis.player.domain.model.RepeatMode
import com.auralis.player.domain.model.Song
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
        val viewModel = PlayerViewModel(repository)

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
        val viewModel = PlayerViewModel(repository)

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
        val viewModel = PlayerViewModel(repository)

        viewModel.togglePlayPause()

        assertTrue(repository.playCalled)
    }

    private class FakePlaybackRepository : PlaybackRepository {
        private val _state = MutableStateFlow(PlaybackState.IDLE)
        var playCalled = false
        var pauseCalled = false

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
    }
}
