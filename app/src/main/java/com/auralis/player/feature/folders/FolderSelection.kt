package com.auralis.player.feature.folders

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auralis.player.core.common.format.folderDisplayName
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens

/**
 * Lanzador del selector de carpetas del sistema (Storage Access Framework).
 * Devuelve una funcion que abre el explorador de archivos y, al elegir una
 * carpeta, entrega su URI de arbol.
 */
@Composable
fun rememberFolderPickerLauncher(onPicked: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            onPicked(uri.toString())
        }
    }
    return remember(launcher) { { launcher.launch(null) } }
}

/**
 * Hoja inferior para gestionar las carpetas de musica: agrega carpetas con el
 * explorador de archivos y permite quitarlas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicFoldersSheet(
    folders: Collection<String>,
    onAddFolder: () -> Unit,
    onRemoveFolder: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sorted = remember(folders) { folders.sortedBy { folderDisplayName(it).lowercase() } }

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
            Text(
                text = "CARPETAS DE MÚSICA",
                style = AppType.label.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(spacing.xs))
            Text(
                text = "Agrega carpetas con tu música. Se suman a las canciones que Auralis ya detecta en tu dispositivo.",
                style = AppType.body.copy(fontSize = 13.sp),
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(spacing.m))

            if (sorted.isEmpty()) {
                Text(
                    text = "Aún no has elegido ninguna carpeta.",
                    style = AppType.body,
                    color = colors.textSecondary.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = spacing.s)
                )
            } else {
                sorted.forEach { folder ->
                    FolderEntry(
                        name = folderDisplayName(folder),
                        onRemove = { onRemoveFolder(folder) },
                    )
                    Spacer(modifier = Modifier.height(spacing.s))
                }
            }

            Spacer(modifier = Modifier.height(spacing.s))

            Surface(
                onClick = onAddFolder,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(percent = 50),
                color = colors.accent,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(spacing.s))
                    Text(
                        text = "Añadir carpeta",
                        style = AppType.label.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun FolderEntry(
    name: String,
    onRemove: () -> Unit,
) {
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ReproductorThemeTokens.shapes.medium)
            .background(colors.surfaceRaised)
            .padding(horizontal = spacing.m, vertical = spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.FolderOpen,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(spacing.m))
        Text(
            text = name,
            style = AppType.body.copy(fontWeight = FontWeight.Medium),
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = "Quitar carpeta",
            tint = colors.textSecondary,
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onRemove),
        )
    }
}
