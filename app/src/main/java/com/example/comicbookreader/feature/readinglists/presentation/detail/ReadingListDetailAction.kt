package com.example.comicbookreader.feature.readinglists.presentation.detail

sealed interface ReadingListDetailAction {
    data class OnItemClick(val comicId: Int) : ReadingListDetailAction
}