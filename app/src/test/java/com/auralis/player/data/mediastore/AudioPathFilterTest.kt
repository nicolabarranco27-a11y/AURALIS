package com.auralis.player.data.mediastore

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioPathFilterTest {

    @Test
    fun `excluye audio de WhatsApp moderno`() {
        val path = "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Audio/PTT-20240101.opus"

        assertTrue(isNonMusicAudioPath(path))
    }

    @Test
    fun `excluye audio de WhatsApp legacy`() {
        val path = "WhatsApp/Media/WhatsApp Voice Notes/nota.opus"

        assertTrue(isNonMusicAudioPath(path))
    }

    @Test
    fun `excluye audio de Telegram`() {
        val path = "Android/media/org.telegram.messenger/Telegram/Telegram Audio/voice.ogg"

        assertTrue(isNonMusicAudioPath(path))
    }

    @Test
    fun `excluye grabaciones de voz y llamadas`() {
        assertTrue(isNonMusicAudioPath("Recordings/call.m4a"))
        assertTrue(isNonMusicAudioPath("MIUI/sound_recorder/rec0001.m4a"))
        assertTrue(isNonMusicAudioPath("Call Recordings/2024.m4a"))
    }

    @Test
    fun `excluye sonidos del sistema`() {
        assertTrue(isNonMusicAudioPath("Ringtones/default.mp3"))
        assertTrue(isNonMusicAudioPath("Notifications/ping.ogg"))
        assertTrue(isNonMusicAudioPath("Alarms/wake.mp3"))
    }

    @Test
    fun `no excluye musica normal`() {
        assertFalse(isNonMusicAudioPath("Music/Album/track01.mp3"))
        assertFalse(isNonMusicAudioPath("Download/cancion.mp3"))
        assertFalse(isNonMusicAudioPath("Mi Musica/artista/tema.flac"))
    }

    @Test
    fun `no excluye cuando no hay ruta`() {
        assertFalse(isNonMusicAudioPath(null))
        assertFalse(isNonMusicAudioPath(""))
    }
}
