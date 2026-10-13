package com.auralis.player.data.repository

import com.auralis.player.domain.repository.LibraryFolderRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class DefaultLibraryFolderRepository @Inject constructor(
    private val folderPreferences: MusicFolderPreferences,
) : LibraryFolderRepository {

    override fun observeSelectedFolders(): Flow<Set<String>> = folderPreferences.selectedFolders

    override suspend fun selectedFolders(): Set<String> = folderPreferences.selectedFolders()

    override suspend fun addFolder(treeUri: String) {
        folderPreferences.setSelectedFolders(folderPreferences.selectedFolders() + treeUri)
    }

    override suspend fun removeFolder(treeUri: String) {
        folderPreferences.setSelectedFolders(folderPreferences.selectedFolders() - treeUri)
    }
}
