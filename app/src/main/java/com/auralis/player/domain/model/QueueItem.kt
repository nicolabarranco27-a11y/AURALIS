package com.auralis.player.domain.model

/**
 * Elemento de la cola de reproduccion.
 * La cola del dominio es independiente del motor de reproduccion
 * concreto; la traduccion a Media3 ocurre en la capa de playback.
 */
data class QueueItem(
    val uid: String,
    val song: Song,
)
