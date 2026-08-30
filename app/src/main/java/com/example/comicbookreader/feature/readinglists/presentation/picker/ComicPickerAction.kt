package com.example.comicbookreader.feature.readinglists.presentation.picker

sealed interface ComicPickerAction {
    data class OnSearchQueryChanged(val query: String) : ComicPickerAction
    data class OnComicToggled(val comicId: Int) : ComicPickerAction
    data object OnDoneClick : ComicPickerAction
    data object OnBackClick : ComicPickerAction
}