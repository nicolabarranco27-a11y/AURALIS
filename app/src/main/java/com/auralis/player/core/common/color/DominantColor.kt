package com.auralis.player.core.common.color

/**
 * Color RGB (0..255 por canal), independiente de Android.
 */
data class RgbColor(val red: Int, val green: Int, val blue: Int) {
    /** Empaqueta el color como ARGB opaco. */
    fun toArgb(): Int =
        (0xFF shl 24) or ((red and 0xFF) shl 16) or ((green and 0xFF) shl 8) or (blue and 0xFF)

    internal val saturation: Float
        get() {
            val max = maxOf(red, green, blue)
            val min = minOf(red, green, blue)
            return if (max == 0) 0f else (max - min).toFloat() / max
        }
}

private const val CHANNEL_MIN = 40
private const val MIN_SATURATION = 0.12f
private const val ALPHA_THRESHOLD = 128
private const val SHIFT = 4

/**
 * Calcula el color dominante de un buffer de pixeles ARGB.
 *
 * Estrategia: cuantiza cada canal a [SHIFT] bits, agrupa por cubo y puntua
 * cada grupo por frecuencia ponderada por saturacion. Se descartan pixeles
 * transparentes, muy oscuros y practicamente grises, de modo que un artwork
 * en escala de grises devuelve null (el llamador aplica su color de respaldo).
 */
fun dominantColorFromPixels(pixels: IntArray): RgbColor? {
    if (pixels.isEmpty()) return null

    val buckets = HashMap<Int, Bucket>()
    for (pixel in pixels) {
        val alpha = (pixel ushr 24) and 0xFF
        if (alpha < ALPHA_THRESHOLD) continue

        val red = (pixel ushr 16) and 0xFF
        val green = (pixel ushr 8) and 0xFF
        val blue = pixel and 0xFF

        val max = maxOf(red, green, blue)
        val min = minOf(red, green, blue)
        if (max < CHANNEL_MIN) continue

        val saturation = if (max == 0) 0f else (max - min).toFloat() / max
        if (saturation < MIN_SATURATION) continue

        val key = ((red shr SHIFT) shl 8) or ((green shr SHIFT) shl 4) or (blue shr SHIFT)
        buckets.getOrPut(key) { Bucket() }.add(red, green, blue)
    }

    if (buckets.isEmpty()) return null

    var best: Bucket? = null
    var bestScore = -1f
    for (bucket in buckets.values) {
        val average = bucket.average()
        val score = bucket.count * (0.5f + average.saturation)
        if (score > bestScore) {
            bestScore = score
            best = bucket
        }
    }

    return best?.average()
}

/** Acumulador de un cubo de color cuantizado. */
private class Bucket {
    var count = 0
    private var sumRed = 0L
    private var sumGreen = 0L
    private var sumBlue = 0L

    fun add(red: Int, green: Int, blue: Int) {
        count++
        sumRed += red
        sumGreen += green
        sumBlue += blue
    }

    fun average(): RgbColor = RgbColor(
        red = (sumRed / count).toInt(),
        green = (sumGreen / count).toInt(),
        blue = (sumBlue / count).toInt(),
    )
}
