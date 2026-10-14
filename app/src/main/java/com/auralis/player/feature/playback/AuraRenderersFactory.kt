package com.auralis.player.feature.playback

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.AudioCapabilities
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink

/**
 * [DefaultRenderersFactory] que inyecta un [AudioProcessor] en el audio sink de
 * ExoPlayer para poder leer la amplitud del audio sin permisos. Reproduce la
 * configuracion por defecto y solo anade el procesador de ritmo.
 */
@OptIn(UnstableApi::class)
class AuraRenderersFactory(
    context: Context,
    private val processor: AudioProcessor,
) : DefaultRenderersFactory(context) {

    @Suppress("DEPRECATION")
    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioTrackPlaybackParams: Boolean,
    ): AudioSink {
        return DefaultAudioSink.Builder(context)
            .setAudioCapabilities(AudioCapabilities.getCapabilities(context))
            .setEnableFloatOutput(enableFloatOutput)
            .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
            .setAudioProcessors(arrayOf(processor))
            .build()
    }
}
