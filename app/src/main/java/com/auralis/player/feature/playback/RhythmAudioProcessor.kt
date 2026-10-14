package com.auralis.player.feature.playback

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import com.auralis.player.core.common.audio.PlaybackRhythm
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow

/**
 * Procesador de audio passthrough que detecta el **ritmo** (golpes de bateria /
 * graves) del audio que ya circula por ExoPlayer y lo publica en
 * [PlaybackRhythm].
 *
 * No altera la senal: copia la entrada a la salida sin cambios. Solo analiza
 * cuando el formato de entrada es PCM de 16 bits.
 *
 * Cadena de analisis (por buffer):
 *  1. Media de todos los canales -> senal mono.
 *  2. Banda de **graves** (filtro paso bajo ~150 Hz -> bombo/bajo) y banda
 *     **aguda** (mono - graves -> caja/platillos), donde vive el "latido".
 *  3. Envolvente de pico por banda, con caida rapida.
 *  4. Deteccion de **ataques** por banda (pico > media adaptativa * ratio).
 *  5. Un "pulso" que se dispara con cada ataque y se apaga con una **caida
 *     adaptada al tempo** (estimado del intervalo entre ataques), para que el
 *     aura siga el ritmo real y no solo el volumen. Se suma un componente
 *     continuo de volumen para que nunca quede muerta entre golpes.
 */
@OptIn(UnstableApi::class)
class RhythmAudioProcessor : BaseAudioProcessor() {

    private var analyzeSamples: Boolean = false
    private var channels: Int = 2
    private var sampleRate: Int = 44_100
    private var lowPassAlpha: Float = 0.975f

    // Filtro paso bajo (banda de graves).
    private var lowPass: Float = 0f
    private var bassEnvelope: Float = 0f
    private var highEnvelope: Float = 0f
    private var bassReference: Float = 0.05f
    private var highReference: Float = 0.05f
    private var bassAverage: Float = 0f
    private var highAverage: Float = 0f
    private var punch: Float = 0f
    private var secondsSinceOnset: Float = 0f
    private var beatPeriod: Float = 0.5f

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        analyzeSamples = inputAudioFormat.encoding == C.ENCODING_PCM_16BIT
        channels = inputAudioFormat.channelCount.coerceIn(1, 8)
        sampleRate = inputAudioFormat.sampleRate.coerceAtLeast(8_000)
        lowPassAlpha = exp(-2.0 * Math.PI * BASS_CUTOFF_HZ / sampleRate).toFloat()
        resetState()
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val size = inputBuffer.remaining()
        if (size == 0) return

        if (analyzeSamples) {
            analyze(inputBuffer)
        }

        val output = replaceOutputBuffer(size)
        output.put(inputBuffer)
        output.flip()
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onFlush() {
        super.onFlush()
        // Conserva las referencias del AGC para no re-normalizar tras un seek.
        lowPass = 0f
        bassEnvelope = 0f
        highEnvelope = 0f
        bassAverage = 0f
        highAverage = 0f
        punch = 0f
        secondsSinceOnset = 0f
    }

    override fun onReset() {
        super.onReset()
        analyzeSamples = false
        channels = 2
        sampleRate = 44_100
        resetState()
        PlaybackRhythm.reset()
    }

    private fun resetState() {
        lowPass = 0f
        bassEnvelope = 0f
        highEnvelope = 0f
        bassReference = 0.05f
        highReference = 0.05f
        bassAverage = 0f
        highAverage = 0f
        punch = 0f
        secondsSinceOnset = 0f
        beatPeriod = 0.5f
    }

    private fun analyze(buffer: ByteBuffer) {
        val start = buffer.position()
        val end = buffer.limit()
        val bytesPerFrame = channels * 2
        if (bytesPerFrame <= 0) return

        var peakBass = 0f
        var peakHigh = 0f
        var frames = 0
        var frameStart = start
        while (frameStart + bytesPerFrame <= end) {
            var acc = 0f
            var c = 0
            while (c < channels) {
                acc += buffer.getShort(frameStart + c * 2) / 32768f
                c++
            }
            val mono = acc / channels
            lowPass = lowPassAlpha * lowPass + (1f - lowPassAlpha) * mono
            val high = mono - lowPass
            val bassMag = abs(lowPass)
            val highMag = abs(high)
            if (bassMag > peakBass) peakBass = bassMag
            if (highMag > peakHigh) peakHigh = highMag
            frames++
            frameStart += bytesPerFrame
        }
        if (frames == 0) return

        // Envolventes de pico por banda (sube al instante, decae entre golpes).
        bassEnvelope = if (peakBass > bassEnvelope) peakBass else bassEnvelope * BASS_ENV_DECAY
        highEnvelope = if (peakHigh > highEnvelope) peakHigh else highEnvelope * HIGH_ENV_DECAY

        // AGC lento: normaliza volumenes distintos entre canciones.
        bassReference = if (peakBass > bassReference) peakBass else (bassReference * REF_DECAY).coerceAtLeast(REF_FLOOR)
        highReference = if (peakHigh > highReference) peakHigh else (highReference * REF_DECAY).coerceAtLeast(REF_FLOOR)

        val loudBass = (bassEnvelope / bassReference).coerceIn(0f, 1f)
        val loudHigh = (highEnvelope / highReference).coerceIn(0f, 1f)
        val base = loudBass * BASS_LOUD_WEIGHT + loudHigh * HIGH_LOUD_WEIGHT

        // Medias adaptativas para detectar ataques por encima del ruido de fondo.
        bassAverage = bassAverage * (1f - AVG_DECAY) + peakBass * AVG_DECAY
        highAverage = highAverage * (1f - AVG_DECAY) + peakHigh * AVG_DECAY
        val onsetBass = peakBass > bassAverage * ONSET_BASS_RATIO && peakBass > MIN_BASS
        val onsetHigh = peakHigh > highAverage * ONSET_HIGH_RATIO && peakHigh > MIN_HIGH

        val bufferSeconds = frames.toFloat() / sampleRate
        secondsSinceOnset += bufferSeconds
        if (onsetBass || onsetHigh) {
            if (secondsSinceOnset in MIN_BEAT_PERIOD..MAX_BEAT_PERIOD) {
                beatPeriod = secondsSinceOnset
            }
            secondsSinceOnset = 0f
            punch = 1f
        }

        // Caida del pulso adaptada al tempo: se apaga hasta PUNCH_FLOOR en un beat.
        val decay = PUNCH_FLOOR.toDouble().pow((bufferSeconds / beatPeriod).coerceIn(0.02f, 2f).toDouble()).toFloat()
        punch *= decay

        val level = (base * BASE_WEIGHT + punch * PUNCH_WEIGHT).coerceIn(0f, 1f)
        PlaybackRhythm.update(level)
    }

    private companion object {
        const val BASS_CUTOFF_HZ = 150.0
        const val BASS_ENV_DECAY = 0.80f
        const val HIGH_ENV_DECAY = 0.68f
        const val REF_DECAY = 0.996f
        const val REF_FLOOR = 0.004f
        const val AVG_DECAY = 0.02f
        const val ONSET_BASS_RATIO = 1.40f
        const val ONSET_HIGH_RATIO = 1.75f
        const val MIN_BASS = 0.012f
        const val MIN_HIGH = 0.025f
        const val MIN_BEAT_PERIOD = 0.18f
        const val MAX_BEAT_PERIOD = 1.40f
        const val PUNCH_FLOOR = 0.16f
        const val BASE_WEIGHT = 0.55f
        const val PUNCH_WEIGHT = 0.80f
        const val BASS_LOUD_WEIGHT = 0.55f
        const val HIGH_LOUD_WEIGHT = 0.30f
    }
}
