package com.auralis.player.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
 * Implementa el sistema de fallback global utilizando el logo de EstalingradoCorp.
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
    
    Box(
        modifier = modifier.background(colors.surfaceRaised),
        contentAlignment = Alignment.Center
    ) {
        if (artworkReference != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(artworkReference)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                error = fallbackPainter,
                fallback = fallbackPainter
            )
        } else {
            Image(
                painter = fallbackPainter,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        }
    }
}
