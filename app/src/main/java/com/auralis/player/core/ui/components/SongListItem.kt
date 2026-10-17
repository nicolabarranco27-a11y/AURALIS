// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auralis.player.core.common.format.formatDuration
import com.auralis.player.core.ui.theme.AppColors
import com.auralis.player.core.ui.theme.AppSpacing
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import com.auralis.player.domain.model.Song
import kotlinx.coroutines.launch

/**
 * Fila de cancion reutilizable por TODAS las listas de la app.
 *
 * - Tap: [onClick] (reproducir).
 * - Long-press: [onLongClick] (abre el menu de acciones, p. ej. ocultar,
 *   anadir a playlist, anadir a la cola).
 * - Swipe a la derecha: [onSwipeRight] (anadir a la cola).
 * - Corazon: [onToggleFavorite].
 *
 * [trailingContent] permite acciones especificas de contexto (p. ej. quitar
 * de una playlist o reordenar) sin duplicar el layout.
 */
@Composable
fun SongListItem(
    song: Song,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSwipeRight: () -> Unit,
    onToggleFavorite: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing,
    modifier: Modifier = Modifier,
    showFavorite: Boolean = true,
    trailingContent: (@Composable RowScope.() -> Unit)? = null,
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX = (offsetX + dragAmount).coerceIn(0f, 300f)
                    },
                    onDragEnd = {
                        if (offsetX > 150f) {
                            onSwipeRight()
                        }
                        scope.launch {
                            Animatable(offsetX).animateTo(
                                targetValue = 0f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                            ) {
                                offsetX = value
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            Animatable(offsetX).animateTo(0f) {
                                offsetX = value
                            }
                        }
                    }
                )
            }
            .background(
                if (offsetX > 0) {
                    colors.accent.copy(alpha = 0.2f * (offsetX / 150f).coerceAtMost(1f))
                } else {
                    Color.Transparent
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationX = offsetX }
                .background(if (isCurrent) colors.surfaceRaised.copy(alpha = 0.5f) else colors.background)
                .combinedClickable(
                    enabled = offsetX == 0f,
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .padding(horizontal = spacing.l, vertical = spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuralisArtwork(
                modifier = Modifier
                    .size(44.dp)
                    .clip(ReproductorThemeTokens.shapes.small),
                artworkReference = song.coverReference
            )

            Spacer(modifier = Modifier.width(spacing.m))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    style = AppType.body.copy(fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium),
                    color = if (isCurrent) colors.accent else colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = (song.artist ?: "Artista desconocido").uppercase(),
                    style = AppType.label.copy(fontSize = 10.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Normal),
                    color = if (isCurrent) colors.accentSecondary.copy(alpha = 0.7f) else colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(spacing.s))

            song.durationMs?.let { duration ->
                Text(
                    text = formatDuration(duration),
                    style = AppType.timer,
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }

            if (showFavorite) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (song.isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                        tint = if (song.isFavorite) Color(0xFFE53935) else colors.textSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            trailingContent?.invoke(this)
        }
    }
}
