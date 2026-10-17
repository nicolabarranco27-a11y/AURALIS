// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.auralis.player.core.ui.theme.AppColors
import com.auralis.player.core.ui.theme.AppSpacing
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens

/** Dialogo para crear una playlist (nombre + descripcion opcional). */
@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String?) -> Unit,
    colors: AppColors = ReproductorThemeTokens.colors,
    spacing: AppSpacing = ReproductorThemeTokens.spacing,
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceRaised,
        title = { Text(text = "Nueva Playlist", style = AppType.title, color = colors.textPrimary) },
        text = {
            Column {
                PlaylistTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Nombre de la playlist",
                    colors = colors,
                )
                Spacer(modifier = Modifier.height(spacing.s))
                PlaylistTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Descripción (opcional)",
                    colors = colors,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), description.trim().ifBlank { null }) },
                enabled = name.isNotBlank(),
            ) {
                Text(
                    text = "CREAR",
                    style = AppType.label,
                    color = if (name.isNotBlank()) colors.accent else colors.textSecondary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", style = AppType.label, color = colors.textSecondary)
            }
        },
    )
}

/** Dialogo para renombrar y/o editar la descripcion de una playlist. */
@Composable
fun EditPlaylistDialog(
    currentName: String,
    currentDescription: String?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String?) -> Unit,
    colors: AppColors = ReproductorThemeTokens.colors,
    spacing: AppSpacing = ReproductorThemeTokens.spacing,
) {
    var name by remember { mutableStateOf(currentName) }
    var description by remember { mutableStateOf(currentDescription.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceRaised,
        title = { Text(text = "Editar Playlist", style = AppType.title, color = colors.textPrimary) },
        text = {
            Column {
                PlaylistTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Nombre",
                    colors = colors,
                )
                Spacer(modifier = Modifier.height(spacing.s))
                PlaylistTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Descripción (opcional)",
                    colors = colors,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), description.trim().ifBlank { null }) },
                enabled = name.isNotBlank(),
            ) {
                Text("GUARDAR", style = AppType.label, color = colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", style = AppType.label, color = colors.textSecondary)
            }
        },
    )
}

/** Dialogo de confirmacion destructiva (p. ej. eliminar una playlist). */
@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    colors: AppColors = ReproductorThemeTokens.colors,
    confirmLabel: String = "ELIMINAR",
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceRaised,
        title = { Text(text = title, style = AppType.title, color = colors.textPrimary) },
        text = { Text(text = message, style = AppType.body, color = colors.textSecondary) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, style = AppType.label, color = colors.onError)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", style = AppType.label, color = colors.textSecondary)
            }
        },
    )
}

@Composable
private fun PlaylistTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    colors: AppColors,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = AppType.body) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = colors.accent,
            unfocusedIndicatorColor = colors.outline,
            cursorColor = colors.accent,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
        ),
        textStyle = AppType.body.copy(fontSize = 14.sp),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}