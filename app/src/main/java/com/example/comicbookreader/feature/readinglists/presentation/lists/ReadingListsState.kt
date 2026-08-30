package com.example.comicbookreader.feature.readinglists.presentation.lists

import com.example.comicbookreader.core.domain.model.ReadingList

data class ReadingListsState(
    val lists: List<ReadingList> = emptyList(),
    val showCreateDialog: Boolean = false,
    val listPendingRename: ReadingList? = null,
    val listPendingDelete: ReadingList? = null,
    val menuOpenForListId: Long? = null
)