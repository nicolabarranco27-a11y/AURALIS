package com.auralis.player.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Escala de espaciado base 4dp. */
@Immutable
data class AppSpacing(
    val xs: Dp,
    val s: Dp,
    val m: Dp,
    val l: Dp,
    val xl: Dp,
)

val DarkAppSpacing = AppSpacing(
    xs = 4.dp,
    s = 8.dp,
    m = 16.dp,
    l = 24.dp,
    xl = 32.dp,
)
