package com.auralis.player.core.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import kotlinx.coroutines.delay

@Composable
fun QueueFeedbackOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing

    LaunchedEffect(visible) {
        if (visible) {
            delay(1200)
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 100.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(300)) + scaleIn(tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)),
            exit = fadeOut(tween(300)) + scaleOut(tween(300))
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.accent.copy(alpha = 0.75f))
                    .padding(horizontal = spacing.l, vertical = spacing.m),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        style = AppType.title.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(spacing.s))
                    Text(
                        text = "AÑADIDA A LA COLA",
                        color = Color.White,
                        style = AppType.label.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
