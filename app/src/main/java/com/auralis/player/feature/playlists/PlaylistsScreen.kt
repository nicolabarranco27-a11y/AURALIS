package com.auralis.player.feature.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auralis.player.core.common.format.formatSongCount
import com.auralis.player.core.common.format.formatTotalDuration
import com.auralis.player.core.ui.components.AuralisArtwork
import com.auralis.player.core.ui.components.ConfirmDeleteDialog
import com.auralis.player.core.ui.components.CreatePlaylistDialog
import com.auralis.player.core.ui.components.EditPlaylistDialog
import com.auralis.player.core.ui.components.LoadingState
import com.auralis.player.core.ui.theme.AppColors
import com.auralis.player.core.ui.theme.AppSpacing
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import com.auralis.player.domain.model.PlaylistSummary

@Composable
fun PlaylistsScreen(
    onMenuClick: () -> Unit,
    onPlaylistClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing

    var showCreateDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<PlaylistSummary?>(null) }
    var deleting by remember { mutableStateOf<PlaylistSummary?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            PlaylistsHeader(
                onMenuClick = onMenuClick,
                colors = colors,
                spacing = spacing
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = colors.accent,
                shape = ReproductorThemeTokens.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Crear playlist",
                    tint = Color.White
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> {
                    LoadingState(colors = colors)
                }
                state.playlists.isEmpty() -> {
                    EmptyPlaylistsState(
                        onCreateClick = { showCreateDialog = true },
                        colors = colors,
                        spacing = spacing
                    )
                }
                else -> {
                    PlaylistsList(
                        playlists = state.playlists,
                        onPlaylistClick = onPlaylistClick,
                        onEditClick = { editing = it },
                        onDeleteClick = { deleting = it },
                        colors = colors,
                        spacing = spacing
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, description ->
                viewModel.createPlaylist(name, description)
                showCreateDialog = false
            },
            colors = colors,
            spacing = spacing
        )
    }

    editing?.let { playlist ->
        EditPlaylistDialog(
            currentName = playlist.name,
            currentDescription = playlist.description,
            onDismiss = { editing = null },
            onConfirm = { name, description ->
                viewModel.editPlaylist(playlist.id, name, description)
                editing = null
            },
            colors = colors,
            spacing = spacing
        )
    }

    deleting?.let { playlist ->
        ConfirmDeleteDialog(
            title = "Eliminar playlist",
            message = "¿Seguro que quieres eliminar \"${playlist.name}\"? Esta acción no se puede deshacer.",
            onDismiss = { deleting = null },
            onConfirm = {
                viewModel.deletePlaylist(playlist.id)
                deleting = null
            },
            colors = colors
        )
    }
}

@Composable
private fun PlaylistsHeader(
    onMenuClick: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = spacing.l, vertical = spacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Rounded.Menu,
                contentDescription = "Abrir menú",
                tint = colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.width(spacing.s))

        Text(
            text = "PLAYLISTS",
            style = AppType.display.copy(fontSize = 24.sp),
            color = colors.textPrimary,
            fontWeight = FontWeight.Light,
            letterSpacing = 4.sp
        )
    }
}

@Composable
private fun PlaylistsList(
    playlists: List<PlaylistSummary>,
    onPlaylistClick: (String) -> Unit,
    onEditClick: (PlaylistSummary) -> Unit,
    onDeleteClick: (PlaylistSummary) -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        items(playlists, key = { it.id.value }) { playlist ->
            PlaylistItem(
                playlist = playlist,
                onClick = { onPlaylistClick(playlist.id.value) },
                onEdit = { onEditClick(playlist) },
                onDelete = { onDeleteClick(playlist) },
                colors = colors,
                spacing = spacing
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = spacing.l),
                thickness = 0.5.dp,
                color = colors.outline.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun PlaylistItem(
    playlist: PlaylistSummary,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = spacing.l, end = spacing.s, top = spacing.s, bottom = spacing.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AuralisArtwork(
            modifier = Modifier
                .size(56.dp)
                .clip(ReproductorThemeTokens.shapes.small),
            artworkReference = playlist.coverReference
        )

        Spacer(modifier = Modifier.width(spacing.m))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = AppType.body.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val subtitle = playlist.description?.takeIf { it.isNotBlank() }
                ?: buildString {
                    append(formatSongCount(playlist.songCount))
                    if (playlist.totalDurationMs > 0) {
                        append(" · ")
                        append(formatTotalDuration(playlist.totalDurationMs))
                    }
                }
            Text(
                text = subtitle,
                style = AppType.label.copy(fontSize = 11.sp),
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Opciones",
                    tint = colors.textSecondary
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                containerColor = colors.surfaceRaised
            ) {
                DropdownMenuItem(
                    text = { Text("Editar", style = AppType.body, color = colors.textPrimary) },
                    onClick = {
                        menuExpanded = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Eliminar", style = AppType.body, color = colors.onError) },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyPlaylistsState(
    onCreateClick: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
            contentDescription = null,
            tint = colors.accentDim,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(spacing.m))
        Text(
            text = "Aún no tienes playlists",
            style = AppType.title,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(spacing.xs))
        Text(
            text = "Crea una para agrupar tus canciones favoritas.",
            style = AppType.body,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(spacing.l))
        TextButton(onClick = onCreateClick) {
            Text("CREAR PLAYLIST", style = AppType.label, color = colors.accent)
        }
    }
}