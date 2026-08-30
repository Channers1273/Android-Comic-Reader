package com.example.comicbookreader.feature.collection.presentation.detail

sealed interface CollectionDetailAction {
    data class OnComicClick(val comicId: Int) : CollectionDetailAction
}