package com.example.comicbookreader.feature.readinglists.presentation.lists

import com.example.comicbookreader.core.domain.model.ReadingList

sealed interface ReadingListsAction {
    data class OnListClick(val listId: Long) : ReadingListsAction
    data class OnMenuClick(val listId: Long) : ReadingListsAction
    data object OnMenuDismiss : ReadingListsAction
    data class OnRenameMenuItemClick(val list: ReadingList) : ReadingListsAction
    data class OnEditContentsMenuItemClick(val listId: Long) : ReadingListsAction
    data class OnDeleteMenuItemClick(val list: ReadingList) : ReadingListsAction
    data object OnAddListClick : ReadingListsAction
    data class OnCreateListConfirm(val name: String) : ReadingListsAction
    data object OnCreateDialogDismiss : ReadingListsAction
    data class OnRenameConfirm(val listId: Long, val newName: String) : ReadingListsAction
    data object OnRenameDialogDismiss : ReadingListsAction
    data class OnDeleteConfirm(val listId: Long) : ReadingListsAction
    data object OnDeleteDialogDismiss : ReadingListsAction
}