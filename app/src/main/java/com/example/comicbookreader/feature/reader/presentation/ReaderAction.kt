package com.example.comicbookreader.feature.reader.presentation

sealed interface ReaderAction {
    data class OnPageChanged(val index: Int) : ReaderAction
    data object OnToggleOverlay : ReaderAction
    data object OnContinueToNextComic : ReaderAction
}