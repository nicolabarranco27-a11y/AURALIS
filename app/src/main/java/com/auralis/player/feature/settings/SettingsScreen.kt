package com.auralis.player.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auralis.player.core.common.format.folderDisplayName
import com.auralis.player.core.ui.theme.AppColors
import com.auralis.player.core.ui.theme.AppSpacing
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import com.auralis.player.core.ui.theme.ThemeMode
import com.auralis.player.feature.folders.MusicFoldersSheet
import com.auralis.player.feature.folders.rememberFolderPickerLauncher

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onHiddenSongsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val selectedFolders by viewModel.selectedFolders.collectAsStateWithLifecycle(
        initialValue = emptySet()
    )
    var showFolderSheet by remember { mutableStateOf(false) }
    val openFolderPicker = rememberFolderPickerLauncher { uri -> viewModel.addFolder(uri) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(spacing.l)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Volver",
                    tint = colors.textPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.s))

        // Section: Biblioteca
        Text(
            text = "BIBLIOTECA",
            style = AppType.label.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = spacing.m)
        )

        FolderSelectionCard(
            selectedFolders = selectedFolders,
            onManageFoldersClick = { showFolderSheet = true },
            colors = colors,
            spacing = spacing
        )

        Spacer(modifier = Modifier.height(spacing.m))

        HiddenSongsCard(
            onClick = onHiddenSongsClick,
            colors = colors,
            spacing = spacing
        )

        Spacer(modifier = Modifier.height(spacing.xl))

        // Section: Apariencia
        Text(
            text = "APARIENCIA",
            style = AppType.label.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = spacing.m)
        )

        ThemeSelectionCard(
            selected = themeMode,
            onSelect = { viewModel.setThemeMode(it) },
            colors = colors,
            spacing = spacing
        )
    }

    if (showFolderSheet) {
        MusicFoldersSheet(
            folders = selectedFolders,
            onAddFolder = { openFolderPicker() },
            onRemoveFolder = { viewModel.removeFolder(it) },
            onDismiss = { showFolderSheet = false }
        )
    }
}

@Composable
private fun FolderSelectionCard(
    selectedFolders: Set<String>,
    onManageFoldersClick: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    val folderNames = selectedFolders
        .map { folderDisplayName(it) }
        .sorted()
        .joinToString(", ")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = ReproductorThemeTokens.shapes.medium,
        color = colors.surfaceRaised,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(spacing.m)) {
            Text(
                text = "Carpetas de música",
                style = AppType.body.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(spacing.xs))
            Text(
                text = folderNames.ifEmpty { "Escaneo automático activo. Sin carpetas adicionales." },
                style = AppType.label.copy(fontSize = 12.sp, letterSpacing = 0.5.sp),
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(spacing.m))
            Surface(
                onClick = onManageFoldersClick,
                shape = RoundedCornerShape(percent = 50),
                color = colors.accent
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Gestionar carpetas",
                        style = AppType.label.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun HiddenSongsCard(
    onClick: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ReproductorThemeTokens.shapes.medium,
        color = colors.surfaceRaised,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(spacing.m)) {
            Text(
                text = "Canciones ocultas",
                style = AppType.body.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(spacing.xs))
            Text(
                text = "Revisa y restaura las canciones que ocultaste de tu biblioteca.",
                style = AppType.label.copy(fontSize = 12.sp, letterSpacing = 0.5.sp),
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun ThemeSelectionCard(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    val themeOptions = listOf(
        ThemeMode.LIGHT to "Claro",
        ThemeMode.DARK to "Oscuro",
        ThemeMode.SYSTEM to "Sistema",
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = ReproductorThemeTokens.shapes.medium,
        color = colors.surfaceRaised,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(spacing.m)) {
            Text(
                text = "Tema",
                style = AppType.body.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(spacing.xs))
            Text(
                text = "Actualmente la app usa solo el modo oscuro.",
                style = AppType.label.copy(fontSize = 12.sp, letterSpacing = 0.5.sp),
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(spacing.m))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.s)
            ) {
                themeOptions.forEach { (mode, label) ->
                    val isSelected = mode == selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(if (isSelected) colors.accent else colors.surface)
                            .clickable { onSelect(mode) }
                            .padding(vertical = spacing.s),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = AppType.label.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = if (isSelected) Color.White else colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}
