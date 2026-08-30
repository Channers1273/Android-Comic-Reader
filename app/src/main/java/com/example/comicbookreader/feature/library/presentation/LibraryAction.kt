package com.example.comicbookreader.feature.library.presentation

sealed interface LibraryAction {
    data object OnChangeDirectoryClick : LibraryAction
    data class OnDirectoryPicked(val uriString: String) : LibraryAction
    data class OnFolderClick(val folderPath: String) : LibraryAction
    data class OnComicClick(val comicId: Int) : LibraryAction
    data object OnRefresh : LibraryAction
}