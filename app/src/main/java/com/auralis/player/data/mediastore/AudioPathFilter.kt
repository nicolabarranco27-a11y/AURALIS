package com.auralis.player.data.mediastore

/**
 * Marcadores de ruta que corresponden a audio que NO es musica: voz de
 * mensajeria (WhatsApp, Telegram, Signal), grabaciones de voz/llamadas y
 * sonidos del sistema (tonos, notificaciones, alarmas).
 *
 * Se aplican sobre la ruta relativa de MediaStore (`RELATIVE_PATH`, API 29+)
 * o la ruta absoluta (`DATA`, API < 29), en minusculas y normalizadas con
 * barras inicial y final.
 */
private val NON_MUSIC_PATH_MARKERS = listOf(
    // Audio gestionado por apps (WhatsApp/Telegram/Signal modernos viven aqui).
    "/android/media/",
    "/whatsapp/",
    "/whatsapp audio/",
    "/whatsapp voice notes/",
    "/telegram/",
    "/telegram audio/",
    "/telegram voice/",
    "/signal/",
    // Grabaciones de voz y llamadas.
    "/recordings/",
    "/call recordings/",
    "/audio recordings/",
    "/voice recorder/",
    "/sound recorder/",
    "/voice notes/",
    "/miui/sound_recorder/",
    // Sonidos del sistema (no son musica).
    "/ringtones/",
    "/notifications/",
    "/alarms/",
)

/**
 * `true` si la ruta corresponde a audio que no es musica y debe excluirse
 * del catalogo. Una ruta nula o vacia no se excluye (no hay informacion).
 */
internal fun isNonMusicAudioPath(rawPath: String?): Boolean {
    if (rawPath.isNullOrBlank()) return false
    val normalized = "/" + rawPath.replace('\\', '/').trim('/').lowercase() + "/"
    return NON_MUSIC_PATH_MARKERS.any { normalized.contains(it) }
}
