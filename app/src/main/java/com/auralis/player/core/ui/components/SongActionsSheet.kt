// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auralis.player.core.ui.theme.AppColors
import com.auralis.player.core.ui.theme.AppSpacing
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import com.auralis.player.domain.model.Song

/** Accion mostrada en [SongActionsSheet]. */
data class SongAction(
    val icon: ImageVector,
    val label: String,
    val destructive: Boolean = false,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

/**
 * Acciones estandar de una cancion, compartidas por todas las listas.
 * [extra] permite insertar acciones especificas de contexto (p. ej. quitar
 * de una playlist) al final.
 */
fun songActions(
    song: Song,
    onPlay: () -> Unit,
    onAddToQueue: () -> Unit,
    onToggleFavorite: () -> Unit,
    onHide: () -> Unit,
    extra: List<SongAction> = emptyList(),
): List<SongAction> = buildList {
    add(SongAction(Icons.Rounded.PlayArrow, "Reproducir", onClick = onPlay))
    add(SongAction(Icons.AutoMirrored.Rounded.QueueMusic, "Añadir a la cola", onClick = onAddToQueue))
    add(
        SongAction(
            icon = if (song.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            label = if (song.isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
            onClick = onToggleFavorite,
        )
    )
    addAll(extra)
    add(
        SongAction(
            icon = Icons.Rounded.VisibilityOff,
            label = "Ocultar de la biblioteca",
            destructive = true,
            onClick = onHide,
        )
    )
}

/**
 * Hoja inferior con las acciones disponibles para una cancion
 * (reproducir, anadir a la cola, playlist, ocultar, ...). Se abre con
 * long-press sobre una fila de [SongListItem].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongActionsSheet(
    song: Song,
    actions: List<SongAction>,
    onDismiss: () -> Unit,
    colors: AppColors = ReproductorThemeTokens.colors,
    spacing: AppSpacing = ReproductorThemeTokens.spacing,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.l)
                .padding(bottom = spacing.l)
                .navigationBarsPadding()
        ) {
            // Cabecera con la cancion
            Row(verticalAlignment = Alignment.CenterVertically) {
                AuralisArtwork(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(ReproductorThemeTokens.shapes.small),
                    artworkReference = song.coverReference,
                )
                Spacer(modifier = Modifier.width(spacing.m))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = AppType.body.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = (song.artist ?: "Artista desconocido").uppercase(),
                        style = AppType.label.copy(fontSize = 10.sp, letterSpacing = 1.2.sp),
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.m))
            HorizontalDivider(color = colors.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(spacing.s))

            actions.forEach { action ->
                SongActionRow(
                    action = action,
                    colors = colors,
                    spacing = spacing,
                    onClick = {
                        action.onClick()
                        onDismiss()
                    },
                )
            }
        }
    }
}

@Composable
private fun SongActionRow(
    action: SongAction,
    colors: AppColors,
    spacing: AppSpacing,
    onClick: () -> Unit,
) {
    val tint = when {
        !action.enabled -> colors.textSecondary.copy(alpha = 0.4f)
        action.destructive -> colors.onError
        else -> colors.textPrimary
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ReproductorThemeTokens.shapes.small)
            .clickable(enabled = action.enabled, onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Icon(
            imageVector = action.icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(spacing.m))
        Text(
            text = action.label,
            style = AppType.body,
            color = tint,
        )
    }
}
