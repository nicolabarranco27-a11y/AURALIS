package com.auralis.player.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/** Modo de tema seleccionable por el usuario. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    ;

    companion object {
        fun fromKey(key: String?): ThemeMode =
            entries.firstOrNull { it.name.equals(key, ignoreCase = true) } ?: SYSTEM
    }
}

private val LocalAppColors = staticCompositionLocalOf { DarkAppColors }
private val LocalAppShapes = staticCompositionLocalOf { DarkAppShapes }
private val LocalAppSpacing = staticCompositionLocalOf { DarkAppSpacing }
private val LocalAppMotion = staticCompositionLocalOf { DarkAppMotion }

object ReproductorThemeTokens {
    val colors: AppColors @Composable get() = LocalAppColors.current
    val shapes: AppShapes @Composable get() = LocalAppShapes.current
    val spacing: AppSpacing @Composable get() = LocalAppSpacing.current
    val motion: AppMotion @Composable get() = LocalAppMotion.current
    
    val primaryGradient: androidx.compose.ui.graphics.Brush
        @Composable get() = androidx.compose.ui.graphics.Brush.horizontalGradient(
            colors = listOf(colors.accent, colors.accentSecondary)
        )
}

/**
 * Tema del reproductor. [themeMode] determina si se usa la paleta clara u
 * oscura; [ThemeMode.SYSTEM] delega en la preferencia del sistema operativo.
 * Material 3 actua como infraestructura; la identidad visual se define
 * mediante los tokens propios (AppColors, AppType, AppShapes...).
 */
@Composable
fun ReproductorTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val appColors = if (isDark) DarkAppColors else LightAppColors
    val materialScheme = if (isDark) {
        darkColorScheme(
            primary = appColors.accent,
            secondary = appColors.accentDim,
            background = appColors.background,
            surface = appColors.surface,
            surfaceContainer = appColors.surfaceRaised,
            outline = appColors.outline,
            onBackground = appColors.textPrimary,
            onSurface = appColors.textPrimary,
            onPrimary = appColors.background,
            error = appColors.onError,
        )
    } else {
        lightColorScheme(
            primary = appColors.accent,
            secondary = appColors.accentDim,
            background = appColors.background,
            surface = appColors.surface,
            surfaceContainer = appColors.surfaceRaised,
            outline = appColors.outline,
            onBackground = appColors.textPrimary,
            onSurface = appColors.textPrimary,
            onPrimary = appColors.background,
            error = appColors.onError,
        )
    }

    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalAppShapes provides DarkAppShapes,
        LocalAppSpacing provides DarkAppSpacing,
        LocalAppMotion provides DarkAppMotion,
    ) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = ReproductorTypography,
            content = content,
        )
    }
}
