package com.auralis.player.feature.player

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auralis.player.core.ui.theme.AuraFallbackColor
import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.RepeatMode
import com.auralis.player.domain.repository.ArtworkColorRepository
import com.auralis.player.domain.repository.PlaybackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado del temporizador de sueno. */
sealed interface SleepTimerState {
    data object Off : SleepTimerState
    data class Running(val remainingMs: Long) : SleepTimerState
}

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackRepository: PlaybackRepository,
    private val artworkColorRepository: ArtworkColorRepository,
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playbackRepository.observePlaybackState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlaybackState.IDLE
        )

    private val _sleepTimer = MutableStateFlow<SleepTimerState>(SleepTimerState.Off)
    val sleepTimer: StateFlow<SleepTimerState> = _sleepTimer.asStateFlow()

    /** Color del aura del reproductor, derivado de la caratula actual. */
    private val _artworkAura = MutableStateFlow(AuraFallbackColor)
    val artworkAura: StateFlow<Color> = _artworkAura.asStateFlow()

    private var sleepTimerJob: Job? = null

    init {
        viewModelScope.launch {
            playbackState
                .map { it.currentSong?.coverReference }
                .distinctUntilChanged()
                .collect { reference ->
                    val argb = artworkColorRepository.dominantColorFor(reference)
                    _artworkAura.value = argb?.let { Color(it) } ?: AuraFallbackColor
                }
        }
    }

    fun togglePlayPause() {
        if (playbackState.value.isPlaying) {
            playbackRepository.pause()
        } else {
            playbackRepository.play()
        }
    }

    fun skipNext() {
        playbackRepository.skipNext()
    }

    fun skipPrevious() {
        playbackRepository.skipPrevious()
    }

    fun seekTo(positionMs: Long) {
        playbackRepository.seekTo(positionMs)
    }

    fun toggleShuffle() {
        playbackRepository.setShuffleEnabled(!playbackState.value.shuffleEnabled)
    }

    fun cycleRepeatMode() {
        val nextMode = when (playbackState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        playbackRepository.setRepeatMode(nextMode)
    }

    /** Inicia (o reinicia) el temporizador; al agotarse pausa la reproduccion. */
    fun startSleepTimer(durationMs: Long) {
        sleepTimerJob?.cancel()
        sleepTimerJob = viewModelScope.launch {
            var remaining = durationMs
            _sleepTimer.value = SleepTimerState.Running(remaining)
            while (remaining > 0) {
                delay(TICK_MS)
                remaining -= TICK_MS
                _sleepTimer.value = SleepTimerState.Running(remaining.coerceAtLeast(0L))
            }
            playbackRepository.pause()
            _sleepTimer.value = SleepTimerState.Off
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimer.value = SleepTimerState.Off
    }

    /** Envia los niveles de banda del ecualizador al motor de audio. */
    fun applyEqualizerBands(bands: List<Float>, preset: String) {
        playbackRepository.applyEqualizerBands(bands, preset)
    }

    override fun onCleared() {
        sleepTimerJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val TICK_MS = 1_000L
    }
}
