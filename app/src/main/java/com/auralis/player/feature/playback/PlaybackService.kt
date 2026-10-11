package com.auralis.player.feature.playback

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.auralis.player.data.repository.EqualizerPreferences
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var exoPlayer: Player? = null
    private var activeEqualizer: android.media.audiofx.Equalizer? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, intent, 
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .build()

        exoPlayer = player

        val initialSessionId = player.audioSessionId
        val sessionExtras = android.os.Bundle().apply {
            putInt("audio_session_id", if (initialSessionId > 0) initialSessionId else 0)
        }

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .setExtras(sessionExtras)
            .build()

        if (initialSessionId > 0) {
            setupEqualizer(initialSessionId)
        }

        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                if (audioSessionId > 0) {
                    val currentExtras = mediaSession?.sessionExtras?.let { android.os.Bundle(it) } ?: android.os.Bundle()
                    currentExtras.putInt("audio_session_id", audioSessionId)
                    mediaSession?.sessionExtras = currentExtras

                    setupEqualizer(audioSessionId)
                }
            }
        })
    }

    private fun setupEqualizer(sessionId: Int) {
        try {
            activeEqualizer?.release()
            activeEqualizer = null
        } catch (_: Exception) { }

        try {
            val eq = android.media.audiofx.Equalizer(0, sessionId).apply {
                enabled = true
            }
            activeEqualizer = eq

            val savedBandValues = EqualizerPreferences.getSavedBandValues(this)
            val bandRange = eq.bandLevelRange
            val minMb = bandRange?.getOrNull(0)?.toInt() ?: -1500
            val maxMb = bandRange?.getOrNull(1)?.toInt() ?: 1500
            val numBands = eq.numberOfBands.toInt()

            savedBandValues.forEachIndexed { bandIndex, dbValue ->
                if (bandIndex in 0 until numBands) {
                    val fraction = ((dbValue + 15f) / 30f).coerceIn(0f, 1f)
                    val millibels = (minMb + fraction * (maxMb - minMb)).toInt().toShort()
                    eq.setBandLevel(bandIndex.toShort(), millibels)
                }
            }
        } catch (_: Exception) {
            activeEqualizer = null
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        try {
            activeEqualizer?.release()
            activeEqualizer = null
        } catch (_: Exception) { }
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player?.playWhenReady == false || player?.mediaItemCount == 0) {
            stopSelf()
        }
    }
}
