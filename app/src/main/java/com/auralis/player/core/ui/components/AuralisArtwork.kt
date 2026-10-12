package com.auralis.player.core.ui.components

import android.net.Uri
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.auralis.player.R
import com.auralis.player.core.ui.theme.ReproductorThemeTokens

/**
 * Componente centralizado para mostrar el artwork de canciones, álbumes o playlists.
 * Implementa el sistema de fallback global con la identidad visual de Auralis.
 */
@Composable
fun AuralisArtwork(
    modifier: Modifier = Modifier,
    contentDescription: String? = "Artwork",
    contentScale: ContentScale = ContentScale.Crop,
    artworkReference: Any? = null 
) {
    val colors = ReproductorThemeTokens.colors
    val fallbackPainter = painterResource(id = R.drawable.placeholder_ec)
    val context = LocalContext.current
    
    val validReference = remember(artworkReference) {
        when (artworkReference) {
            is String -> artworkReference
                .takeIf { it.isNotBlank() }
                // Las portadas embebidas pueden haberse guardado como ruta
                // absoluta sin esquema; Coil necesita un URI valido.
                ?.let { ref ->
                    if (!ref.contains("://") && ref.startsWith("/")) {
                        Uri.fromFile(File(ref)).toString()
                    } else {
                        ref
                    }
                }
            else -> artworkReference
        }
    }

    Box(
        modifier = modifier.background(colors.surfaceRaised),
        contentAlignment = Alignment.Center
    ) {
        if (validReference != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(validReference)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                placeholder = fallbackPainter,
                error = fallbackPainter,
                fallback = fallbackPainter
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.surfaceRaised),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = contentDescription,
                    tint = colors.accent.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxSize(0.5f)
                )
            }
        }
    }
}
