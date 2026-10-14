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

/**
 * Paleta clara equivalente: superficies escalonadas sobre blanco,
 * conservando el mismo acento tecnologico de la paleta oscura.
 */
val LightAppColors = AppColors(
    background = Color(0xFFF7F8FA),
    surface = Color(0xFFFFFFFF),
    surfaceRaised = Color(0xFFEDEFF3),
    outline = Color(0xFFD0D4DC),
    textPrimary = Color(0xFF0B0E14),
    textSecondary = Color(0xFF5A606B),
    accent = Color(0xFF2E5BFF),
    accentSecondary = Color(0xFF8A2BE2),
    accentDim = Color(0xFF1A3BB0),
    onError = Color(0xFFD32F2F),
)

/**
 * Color de respaldo del aura del reproductor cuando la caratula no aporta un
 * color util (sin portada o portada en escala de grises). Un rojo tenue,
 * minimalista y con presencia, independiente de la paleta activa.
 */
val AuraFallbackColor = Color(0xFFC62828)
