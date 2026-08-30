package com.example.comicbookreader.feature.library.presentation

import com.example.comicbookreader.core.presentation.UiText

sealed interface LibraryEvent {
    data object RequestDirectoryPicker : LibraryEvent
    data class NavigateToFolder(val folderPath: String) : LibraryEvent
    data class NavigateToReader(val comicId: Int) : LibraryEvent
    data class ShowError(val message: UiText) : LibraryEvent
}