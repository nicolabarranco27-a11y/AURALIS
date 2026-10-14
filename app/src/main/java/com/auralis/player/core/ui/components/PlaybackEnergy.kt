package com.auralis.player.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import com.auralis.player.core.common.audio.PlaybackRhythm
import kotlin.math.exp

/**
 * Señal de "energía" (0f..1f) que da vida al aura mientras hay reproducción y
 * permanece en un reposo tenue cuando está en pausa.
 *
 * Ofrece dos lecturas:
 *  - [value]: señal **rítmica**, con el golpe real de la música ([PlaybackRhythm])
 *    mezclado con un pulso sintético. Se **suaviza en el tiempo** con una
 *    constante distinta para la subida (ataque rápido) y la bajada (caída
 *    fluida), de modo que el aura acelera y desacelera sin saltos entre buffers
 *    de audio.
 *  - [ambient]: oscilación **lenta y continua** (sin relación con los golpes)
 *    para que el aura nunca quede quieta y su respiración sea fluida.
 *
 * Ambas se leen en fase de dibujo (`drawWithCache`), por lo que no provocan
 * recomposiciones por fotograma.
 */
class PlaybackEnergy internal constructor(
    private val fast: State<Float>,
    private val slow: State<Float>,
    private val playing: Boolean,
) {
    private var smoothed: Float = RESTING
    private var lastNanos: Long = 0L

    /** Señal rítmica (golpe real + pulso sintético) suavizada en el tiempo. */
    val value: Float
        get() {
            val target = if (!playing) RESTING else blend()
            val now = System.nanoTime()
            if (lastNanos == 0L) {
                lastNanos = now - FRAME_NANOS
            }
            val dt = ((now - lastNanos) / 1_000_000_000.0).coerceIn(0.0, MAX_STEP_SECONDS)
            lastNanos = now
            val tau = if (target > smoothed) ATTACK_TAU else RELEASE_TAU
            val k = (1.0 - exp(-dt / tau)).toFloat()
            smoothed += (target - smoothed) * k
            return smoothed.coerceIn(0f, 1f)
        }

    /** Movimiento ambiental continuo (0f..1f), independiente del ritmo. */
    val ambient: Float
        get() = AMBIENT_FLOOR + AMBIENT_SPAN * slow.value

    private fun blend(): Float {
        val synthetic = 0.18f + 0.55f * fast.value + 0.27f * slow.value
        val live = PlaybackRhythm.currentLevel()
        return if (live > 0f) {
            // El ritmo real domina; el pulso sintético solo rellena el fondo.
            (synthetic * 0.22f + live * 0.95f).coerceIn(RESTING, 1f)
        } else {
            synthetic
        }
    }

    private companion object {
        const val RESTING = 0.12f
        const val AMBIENT_FLOOR = 0.35f
        const val AMBIENT_SPAN = 0.65f
        const val ATTACK_TAU = 0.05
        const val RELEASE_TAU = 0.22
        const val MAX_STEP_SECONDS = 0.05
        const val FRAME_NANOS = 16_000_000L
    }
}

@Composable
fun rememberPlaybackEnergy(isPlaying: Boolean): PlaybackEnergy {
    val transition = rememberInfiniteTransition(label = "PlaybackEnergy")
    val fast = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "energyFast",
    )
    val slow = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4_800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "energySlow",
    )
    return remember(isPlaying, fast, slow) { PlaybackEnergy(fast, slow, isPlaying) }
}
