package com.auralis.player.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Paleta oscura propia del reproductor.
 * Superficies escalonadas sobre fondo casi negro neutro,
 * con un unico acento tecnologico.
 */
@Immutable
data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val outline: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val accentSecondary: Color,
    val accentDim: Color,
    val onError: Color,
)

val DarkAppColors = AppColors(
    background = Color(0xFF05070A),
    surface = Color(0xFF0E1118),
    surfaceRaised = Color(0xFF171B26),
    outline = Color(0xFF252A38),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFF9499A1),
    accent = Color(0xFF2E5BFF),
    accentSecondary = Color(0xFF8A2BE2),
    accentDim = Color(0xFF1A3BB0),
    onError = Color(0xFFFF5252),
)
