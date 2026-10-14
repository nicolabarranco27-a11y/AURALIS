package com.auralis.player.core.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.Immutable

/** Tokens de movimiento: transiciones sutiles y fluidas. */
@Immutable
data class AppMotion(
    val durationFast: Int,
    val durationNormal: Int,
    val durationSlow: Int,
    val easingStandard: CubicBezierEasing,
    val easingDecelerate: CubicBezierEasing,
    /** Duracion de un ciclo del pulso ambiental (aura del reproductor). */
    val durationAmbient: Int,
    /** Duracion de una vuelta completa de rotacion del aura. */
    val durationRotation: Int,
    /** Deriva media de las brumas del aura. */
    val durationAuroraDrift: Int,
    /** Deriva lenta de las brumas del aura. */
    val durationAuroraSway: Int,
)

val DarkAppMotion = AppMotion(
    durationFast = 150,
    durationNormal = 250,
    durationSlow = 400,
    easingStandard = CubicBezierEasing(0.2f, 0f, 0f, 1f),
    easingDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f),
    durationAmbient = 2_600,
    durationRotation = 13_000,
    durationAuroraDrift = 21_000,
    durationAuroraSway = 32_000,
)
