package com.auralis.player.feature.settings

import androidx.lifecycle.ViewModel
import com.auralis.player.core.ui.theme.ThemeMode
import com.auralis.player.data.repository.ThemeController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeController: ThemeController,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themeController.themeMode

    fun setThemeMode(mode: ThemeMode) {
        themeController.setThemeMode(mode)
    }
}
