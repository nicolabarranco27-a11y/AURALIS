package com.auralis.player.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Decora una caratula circular con un aura de color derivada de la portada.
 *
 * El diseno es organico y "vivo": una luz ambiental amplia (un disco de luz,
 * un halo con pico en el borde de la caratula y tres brumas de color) derivada
 * de la portada que orbita a velocidades distintas y late al ritmo de una
 * energia sintetica de reproduccion. Al pausar, la energia cae y las brumas
 * quedan casi congeladas y tenues.
 *
 * El color de la portada se satura y aclara (HSV) para que el aura se note
 * aunque la caratula sea oscura o apagada. No captura audio real (sin permisos)
 * ni usa desenfoques reales: todo se dibuja con gradientes suaves leidos en
 * fase de dibujo, sin costo de recomposicion por fotograma.
 *
 * El contenido (la caratula) se dibuja por encima.
 */
@Composable
fun VinylAura(
    isPlaying: Boolean,
    auraColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val energy = rememberPlaybackEnergy(isPlaying)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isPlaying) {
            ReactiveAura(auraColor, energy, Modifier.fillMaxSize())
        } else {
            StaticAura(auraColor, Modifier.fillMaxSize())
        }
        content()
    }
}

/** Aura en reposo: disco de luz suave y brumas estaticas para dar profundidad. */
@Composable
private fun StaticAura(color: Color, modifier: Modifier) {
    Box(
        modifier = modifier.drawWithCache {
            val layers = auraLayers(size = size, color = color)
            onDrawBehind {
                drawCircle(
                    brush = layers.glow,
                    radius = layers.radius,
                    center = layers.center,
                    alpha = 0.90f,
                )
                drawCircle(
                    brush = layers.halo,
                    radius = layers.radius,
                    center = layers.center,
                    alpha = 0.90f,
                )
                drawCircle(
                    brush = layers.blobA.brush,
                    radius = layers.blobA.radius * 0.94f,
                    center = layers.blobA.center,
                    alpha = 0.42f,
                )
                drawCircle(
                    brush = layers.blobB.brush,
                    radius = layers.blobB.radius * 0.94f,
                    center = layers.blobB.center,
                    alpha = 0.36f,
                )
                drawCircle(
                    brush = layers.blobC.brush,
                    radius = layers.blobC.radius * 0.94f,
                    center = layers.blobC.center,
                    alpha = 0.32f,
                )
                drawCircle(
                    brush = layers.blobD.brush,
                    radius = layers.blobD.radius * 0.94f,
                    center = layers.blobD.center,
                    alpha = 0.28f,
                )
            }
        },
    )
}

/** Aura activa: disco de luz que respira y brumas que orbitan y laten con la reproduccion. */
@Composable
private fun ReactiveAura(color: Color, energy: PlaybackEnergy, modifier: Modifier) {
    val motion = ReproductorThemeTokens.motion
    val transition = rememberInfiniteTransition(label = "VinylAura")

    val spinA: State<Float> = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(motion.durationRotation, easing = LinearEasing),
        ),
        label = "auraSpinA",
    )
    val spinB: State<Float> = transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(motion.durationAuroraDrift, easing = LinearEasing),
        ),
        label = "auraSpinB",
    )
    val spinC: State<Float> = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(motion.durationAuroraSway, easing = LinearEasing),
        ),
        label = "auraSpinC",
    )
    val spinD: State<Float> = transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(motion.durationRotation * 2, easing = LinearEasing),
        ),
        label = "auraSpinD",
    )

    Box(
        modifier = modifier.drawWithCache {
            val layers = auraLayers(size = size, color = color)
            onDrawBehind {
                // Lectura en fase de dibujo: evita recomposiciones por fotograma.
                // `e` = golpe (rítmico); `a` = ambiental (deriva continua).
                val e = energy.value
                val a = energy.ambient
                val breathe = 0.96f + 0.05f * a + 0.05f * e
                val pulse = 0.88f + 0.26f * e + 0.03f * a
                val kick = e * 9f

                scale(breathe, pivot = layers.center) {
                    drawCircle(
                        brush = layers.glow,
                        radius = layers.radius * (1f + 0.06f * a + 0.06f * e),
                        center = layers.center,
                        alpha = 0.58f + 0.24f * a + 0.20f * e,
                    )
                    drawCircle(
                        brush = layers.halo,
                        radius = layers.radius * (0.98f + 0.05f * a + 0.08f * e),
                        center = layers.center,
                        alpha = 0.74f + 0.14f * a + 0.14f * e,
                    )

                    rotate(spinA.value + kick, pivot = layers.center) {
                        drawCircle(
                            brush = layers.blobA.brush,
                            radius = layers.blobA.radius * pulse,
                            center = layers.blobA.center,
                            alpha = 0.26f + 0.10f * a + 0.48f * e,
                        )
                    }
                    rotate(spinB.value - kick, pivot = layers.center) {
                        drawCircle(
                            brush = layers.blobB.brush,
                            radius = layers.blobB.radius * pulse,
                            center = layers.blobB.center,
                            alpha = 0.20f + 0.10f * a + 0.44f * e,
                        )
                    }
                    rotate(spinC.value + kick * 0.6f, pivot = layers.center) {
                        drawCircle(
                            brush = layers.blobC.brush,
                            radius = layers.blobC.radius * pulse,
                            center = layers.blobC.center,
                            alpha = 0.17f + 0.09f * a + 0.40f * e,
                        )
                    }
                    rotate(spinD.value - kick * 0.6f, pivot = layers.center) {
                        drawCircle(
                            brush = layers.blobD.brush,
                            radius = layers.blobD.radius * pulse,
                            center = layers.blobD.center,
                            alpha = 0.18f + 0.09f * a + 0.40f * e,
                        )
                    }
                }
            }
        },
    )
}

private class AuraBlob(
    val brush: Brush,
    val center: Offset,
    val radius: Float,
)

private class AuraLayers(
    val center: Offset,
    val radius: Float,
    val glow: Brush,
    val halo: Brush,
    val blobA: AuraBlob,
    val blobB: AuraBlob,
    val blobC: AuraBlob,
    val blobD: AuraBlob,
)

private fun auraLayers(size: Size, color: Color): AuraLayers {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = size.minDimension / 2f

    // El color de la portada suele ser oscuro/apagado: se satura y aclara para
    // que el aura se note de verdad, sin depender de que la caratula sea viva.
    val base = vividly(color)
    val bright = lighten(base, 0.45f)
    val shifted = lighten(rotateHue(base, 42f), 0.15f)

    fun orbit(offset: Float, degrees: Float): Offset {
        val rad = Math.toRadians(degrees.toDouble()).toFloat()
        return Offset(center.x + offset * cos(rad), center.y + offset * sin(rad))
    }

    fun blobAt(blobCenter: Offset, blobColor: Color, blobRadius: Float, coreAlpha: Float): AuraBlob {
        val brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.00f to blobColor.copy(alpha = coreAlpha),
                0.40f to blobColor.copy(alpha = coreAlpha * 0.55f),
                0.72f to blobColor.copy(alpha = coreAlpha * 0.20f),
                1.00f to Color.Transparent,
            ),
            center = blobCenter,
            radius = blobRadius,
        )
        return AuraBlob(brush = brush, center = blobCenter, radius = blobRadius)
    }

    // Disco de luz amplio que colorea toda la zona tras el vinilo.
    val glow = Brush.radialGradient(
        colorStops = arrayOf(
            0.00f to base.copy(alpha = 0.50f),
            0.55f to base.copy(alpha = 0.34f),
            0.82f to base.copy(alpha = 0.14f),
            1.00f to Color.Transparent,
        ),
        center = center,
        radius = radius,
    )

    // Halo con pico justo en el borde de la caratula (0.78/2 = 0.39) para que
    // se lea como un resplandor que emana del vinilo.
    val halo = Brush.radialGradient(
        colorStops = arrayOf(
            0.00f to base.copy(alpha = 0.40f),
            0.34f to base.copy(alpha = 0.60f),
            0.48f to base.copy(alpha = 0.34f),
            0.70f to base.copy(alpha = 0.16f),
            0.88f to base.copy(alpha = 0.06f),
            1.00f to Color.Transparent,
        ),
        center = center,
        radius = radius,
    )

    return AuraLayers(
        center = center,
        radius = radius,
        glow = glow,
        halo = halo,
        blobA = blobAt(orbit(radius * 0.40f, 20f), base, radius * 0.82f, 0.42f),
        blobB = blobAt(orbit(radius * 0.52f, 150f), bright, radius * 0.64f, 0.36f),
        blobC = blobAt(orbit(radius * 0.60f, 268f), shifted, radius * 0.54f, 0.32f),
        blobD = blobAt(orbit(radius * 0.30f, 318f), lighten(rotateHue(base, -48f), 0.28f), radius * 0.46f, 0.30f),
    )
}

private fun lighten(color: Color, amount: Float): Color = Color(
    red = color.red + (1f - color.red) * amount,
    green = color.green + (1f - color.green) * amount,
    blue = color.blue + (1f - color.blue) * amount,
    alpha = color.alpha,
)

private data class Hsv(val hue: Float, val saturation: Float, val value: Float)

private fun Color.toHsv(): Hsv {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val delta = max - min
    var hue = when {
        delta == 0f -> 0f
        max == red -> 60f * (((green - blue) / delta) % 6f)
        max == green -> 60f * (((blue - red) / delta) + 2f)
        else -> 60f * (((red - green) / delta) + 4f)
    }
    if (hue < 0f) hue += 360f
    val saturation = if (max == 0f) 0f else delta / max
    return Hsv(hue, saturation, max)
}

private fun hsvToColor(hue: Float, saturation: Float, value: Float, alpha: Float): Color {
    val h = ((hue % 360f) + 360f) % 360f
    val c = value * saturation
    val x = c * (1f - abs((h / 60f) % 2f - 1f))
    val m = value - c
    val (r, g, b) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(red = r + m, green = g + m, blue = b + m, alpha = alpha)
}

/** Sube saturacion y brillo para que un color de portada apagado aun brille. */
private fun vividly(color: Color): Color {
    val hsv = color.toHsv()
    val saturation = hsv.saturation.coerceIn(0.64f, 1f)
    val value = hsv.value.coerceIn(0.68f, 1f)
    return hsvToColor(hsv.hue, saturation, value, color.alpha)
}

/** Rota el matiz de [color] en [degrees] grados conservando saturacion y valor. */
private fun rotateHue(color: Color, degrees: Float): Color {
    val hsv = color.toHsv()
    return hsvToColor(hsv.hue + degrees, hsv.saturation, hsv.value, color.alpha)
}
