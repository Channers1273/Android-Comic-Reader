package com.example.comicbookreader.feature.readinglists.presentation.editcontents

import com.example.comicbookreader.feature.readinglists.presentation.detail.ReadingListItemUi

data class ReadingListEditContentsState(
    val listId: Long = 0,
    val items: List<ReadingListItemUi> = emptyList()
)