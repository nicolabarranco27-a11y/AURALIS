package com.auralis.player.data.repository

/**
 * Calcula el indice donde insertar una cancion en la cola para mantener
 * el orden de llegada (A -> B -> C).
 *
 * El resultado se acota a [0, itemCount] porque el indice actual de Media3
 * puede quedar desfasado respecto al [lastQueuedIndex] (p. ej. la cola queda
 * vacia o el servicio se recrea), lo que provocaba IndexOutOfBoundsException
 * al insertar.
 */
internal object QueueInsertIndexCalculator {

    fun calculate(currentIndex: Int, lastQueuedIndex: Int, itemCount: Int): Int {
        val base = if (lastQueuedIndex < currentIndex) {
            currentIndex + 1
        } else {
            lastQueuedIndex + 1
        }
        return base.coerceIn(0, itemCount)
    }
}
