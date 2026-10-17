// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.data.repository

import android.content.Context
import android.content.SharedPreferences

object EqualizerPreferences {
    private const val PREFS_NAME = "auralis_equalizer_prefs"
    private const val KEY_PRESET = "saved_preset"
    private const val KEY_BAND_PREFIX = "saved_band_"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSavedPreset(context: Context): String {
        return getPrefs(context).getString(KEY_PRESET, "Normal") ?: "Normal"
    }

    fun getSavedBandValues(context: Context): List<Float> {
        val prefs = getPrefs(context)
        val defaultValues = listOf(0f, 0f, 0f, 0f, 0f)
        return (0..4).map { index ->
            prefs.getFloat("$KEY_BAND_PREFIX$index", defaultValues[index])
        }
    }

    fun saveEqualizerSettings(context: Context, preset: String, bandValues: List<Float>) {
        getPrefs(context).edit().apply {
            putString(KEY_PRESET, preset)
            bandValues.forEachIndexed { index, value ->
                putFloat("$KEY_BAND_PREFIX$index", value)
            }
            apply()
        }
    }
}
