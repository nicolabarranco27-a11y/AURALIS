// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auralis.player.core.ui.theme.ThemeMode
import com.auralis.player.data.repository.ThemeController
import com.auralis.player.data.scanner.LibraryScanScheduler
import com.auralis.player.domain.repository.LibraryFolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeController: ThemeController,
    private val folderRepository: LibraryFolderRepository,
    private val scanScheduler: LibraryScanScheduler,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themeController.themeMode

    val selectedFolders: Flow<Set<String>> = folderRepository.observeSelectedFolders()

    fun setThemeMode(mode: ThemeMode) {
        themeController.setThemeMode(mode)
    }

    fun addFolder(treeUri: String) {
        viewModelScope.launch {
            folderRepository.addFolder(treeUri)
            scanScheduler.requestScan()
        }
    }

    fun removeFolder(treeUri: String) {
        viewModelScope.launch {
            folderRepository.removeFolder(treeUri)
            scanScheduler.requestScan()
        }
    }
}
