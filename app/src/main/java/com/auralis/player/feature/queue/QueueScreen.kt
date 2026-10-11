package com.auralis.player.feature.queue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auralis.player.core.ui.components.AuralisArtwork
import com.auralis.player.core.ui.theme.AppSpacing
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import com.auralis.player.domain.model.Song

@Composable
fun QueueScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QueueViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = spacing.m, vertical = spacing.m),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Cerrar cola",
                        tint = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(spacing.xs))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "COLA",
                        style = AppType.display.copy(fontSize = 24.sp),
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 4.sp
                    )
                    Text(
                        text = "${state.queue.size} en reproducción",
                        style = AppType.label.copy(fontSize = 10.sp),
                        color = colors.textSecondary.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (state.queue.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "La cola está vacía",
                        style = AppType.body,
                        color = colors.textSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = spacing.l)
                ) {
                    itemsIndexed(
                        items = state.queue,
                        key = { index, song -> "$index-${song.id.value}" }
                    ) { index, song ->
                        QueueRow(
                            song = song,
                            isCurrent = index == state.currentIndex,
                            isFirst = index == 0,
                            isLast = index == state.queue.lastIndex,
                            onPlay = { viewModel.onSongClick(index) },
                            onMoveUp = { viewModel.moveQueueItem(index, index - 1) },
                            onMoveDown = { viewModel.moveQueueItem(index, index + 1) },
                            onRemove = { viewModel.removeQueueItem(index) },
                            spacing = spacing
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueRow(
    song: Song,
    isCurrent: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onPlay: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    spacing: AppSpacing,
) {
    val colors = ReproductorThemeTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.m, vertical = spacing.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AuralisArtwork(
            modifier = Modifier.size(44.dp),
            artworkReference = song.coverReference
        )
        Spacer(modifier = Modifier.width(spacing.m))
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onPlay
                )
        ) {
            Text(
                text = song.title,
                style = AppType.body.copy(fontWeight = FontWeight.SemiBold),
                color = if (isCurrent) colors.accent else colors.textPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = song.artist ?: "Unknown",
                style = AppType.label.copy(fontSize = 11.sp),
                color = colors.textSecondary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
            IconButton(onClick = onMoveUp, enabled = !isFirst) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowUp,
                    contentDescription = "Subir en la cola",
                    tint = if (isFirst) colors.textSecondary.copy(alpha = 0.3f) else colors.textSecondary
                )
            }
            IconButton(onClick = onMoveDown, enabled = !isLast) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Bajar en la cola",
                    tint = if (isLast) colors.textSecondary.copy(alpha = 0.3f) else colors.textSecondary
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Quitar de la cola",
                    tint = colors.textSecondary
                )
            }
        }
    }
}
