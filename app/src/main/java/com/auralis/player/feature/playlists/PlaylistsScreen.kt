package com.auralis.player.feature.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.auralis.player.core.ui.theme.AppColors
import com.auralis.player.core.ui.theme.AppSpacing
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import com.auralis.player.domain.model.Playlist

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
                contentColor = Color.White,
                shape = ReproductorThemeTokens.shapes.medium
            ) {
                Text("+", style = AppType.title.copy(fontSize = 24.sp))
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
                    EmptyPlaylistsState(colors = colors)
                }
                else -> {
                    PlaylistsList(
                        playlists = state.playlists,
                        onPlaylistClick = onPlaylistClick,
                        onDeleteClick = { viewModel.deletePlaylist(it) },
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
            onConfirm = { name ->
                viewModel.createPlaylist(name)
                showCreateDialog = false
            },
            colors = colors,
            spacing = spacing
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
            Text(
                text = "☰",
                style = AppType.title.copy(fontSize = 24.sp),
                color = colors.textSecondary
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
    playlists: List<Playlist>,
    onPlaylistClick: (String) -> Unit,
    onDeleteClick: (Playlist) -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(playlists, key = { it.id.value }) { playlist ->
            PlaylistItem(
                playlist = playlist,
                onClick = { onPlaylistClick(playlist.id.value) },
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
    playlist: Playlist,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.l, vertical = spacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(ReproductorThemeTokens.shapes.small)
                .background(colors.surfaceRaised),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "☰",
                color = colors.accent.copy(alpha = 0.6f),
                style = AppType.title.copy(fontSize = 20.sp)
            )
        }

        Spacer(modifier = Modifier.width(spacing.m))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = AppType.body.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Playlist de usuario",
                style = AppType.label.copy(fontSize = 10.sp),
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        IconButton(onClick = onDelete) {
            Text("×", style = AppType.title, color = colors.onError.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceRaised,
        title = {
            Text(
                text = "Nueva Playlist",
                style = AppType.title,
                color = colors.textPrimary
            )
        },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Nombre de la playlist", style = AppType.body) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = colors.accent,
                        unfocusedIndicatorColor = colors.outline,
                        cursorColor = colors.accent,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    textStyle = AppType.body,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text("CREAR", style = AppType.label, color = if (name.isNotBlank()) colors.accent else colors.textSecondary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", style = AppType.label, color = colors.textSecondary)
            }
        }
    )
}

@Composable
private fun LoadingState(colors: AppColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = colors.accent, strokeWidth = 2.dp)
    }
}

@Composable
private fun EmptyPlaylistsState(colors: AppColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No tienes playlists creadas",
            style = AppType.body,
            color = colors.textSecondary
        )
    }
}
