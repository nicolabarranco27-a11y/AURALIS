package com.auralis.player.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auralis.player.core.ui.theme.AppColors
import com.auralis.player.core.ui.theme.AppType

/**
 * Estado de carga compartido. Con [message] nulo muestra solo un indicador
 * centrado; con mensaje, un indicador compacto con etiqueta.
 */
@Composable
fun LoadingState(
    colors: AppColors,
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    if (message == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colors.accent, strokeWidth = 2.dp)
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = colors.accent,
                strokeWidth = 1.dp,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = message.uppercase(),
                style = AppType.label,
                color = colors.accent,
                letterSpacing = 3.sp
            )
        }
    }
}

/** Estado vacio compartido con mensaje centrado. */
@Composable
fun EmptyState(
    message: String,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = AppType.body,
            color = colors.textSecondary
        )
    }
}
