package com.auralis.player.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

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
 * Tema oscuro unico del reproductor.
 * Material 3 actua como infraestructura; la identidad visual
 * se define mediante los tokens propios (AppColors, AppType, AppShapes...).
 */
@Composable
fun ReproductorTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalAppColors provides DarkAppColors,
        LocalAppShapes provides DarkAppShapes,
        LocalAppSpacing provides DarkAppSpacing,
        LocalAppMotion provides DarkAppMotion,
    ) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = DarkAppColors.accent,
                secondary = DarkAppColors.accentDim,
                background = DarkAppColors.background,
                surface = DarkAppColors.surface,
                surfaceContainer = DarkAppColors.surfaceRaised,
                outline = DarkAppColors.outline,
                onBackground = DarkAppColors.textPrimary,
                onSurface = DarkAppColors.textPrimary,
                onPrimary = DarkAppColors.background,
                error = DarkAppColors.onError,
            ),
            typography = ReproductorTypography,
            content = content,
        )
    }
}
