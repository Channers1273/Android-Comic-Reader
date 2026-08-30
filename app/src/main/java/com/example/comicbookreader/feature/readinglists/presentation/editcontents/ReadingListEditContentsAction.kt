package com.example.comicbookreader.feature.readinglists.presentation.editcontents

sealed interface ReadingListEditContentsAction {
    data class OnRemoveItemClick(val itemId: Long) : ReadingListEditContentsAction
    data class OnItemsReordered(val orderedItemIds: List<Long>) : ReadingListEditContentsAction
    data object OnAddComicsClick : ReadingListEditContentsAction
    data object OnBackClick : ReadingListEditContentsAction
}