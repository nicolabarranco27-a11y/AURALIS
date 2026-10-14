package com.auralis.player.core.common.audio

/**
 * Nivel de "ritmo" del audio que se esta reproduciendo, 0f..1f.
 *
 * Lo alimenta [com.auralis.player.feature.playback.RhythmAudioProcessor] desde el
 * hilo de audio de Media3 (analizando el PCM que ya pasa por el reproductor, por
 * lo que **no requiere el permiso `RECORD_AUDIO`**). El procesador aisla la banda
 * de graves y detecta ataques (golpes de bombo/bajo), asi que el valor sube de
 * golpe en las pulsaciones y baja entre ellas. La UI lo lee en fase de dibujo
 * (sin recomposiciones) para que el aura lata al ritmo real de la cancion.
 *
 * El valor se considera "fresco" solo durante una ventana corta; si dejan de
 * llegar muestras (pausa, silencio, fin de pista) cae a 0 para que el aura no se
 * quede encendida.
 */
object PlaybackRhythm {

    private const val FRESH_NANOS = 500_000_000L

    @Volatile
    private var levelValue: Float = 0f

    @Volatile
    private var updatedAtNanos: Long = 0L

    /** Llamado desde el hilo de audio. */
    fun update(value: Float) {
        levelValue = value
        updatedAtNanos = System.nanoTime()
    }

    /** Nivel fresco (0f si no hay datos recientes). Seguro desde cualquier hilo. */
    fun currentLevel(): Float {
        val age = System.nanoTime() - updatedAtNanos
        return if (age in 0 until FRESH_NANOS) levelValue else 0f
    }

    fun reset() {
        levelValue = 0f
        updatedAtNanos = 0L
    }
}
