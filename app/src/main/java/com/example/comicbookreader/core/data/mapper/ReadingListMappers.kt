package com.example.comicbookreader.core.data.mapper

import com.example.comicbookreader.core.database.entity.ReadingListEntity
import com.example.comicbookreader.core.database.entity.ReadingListItemEntity
import com.example.comicbookreader.core.domain.model.ReadingList
import com.example.comicbookreader.core.domain.model.ReadingListItem

fun ReadingListEntity.toReadingList(): ReadingList = ReadingList(
    id = id,
    name = name,
    createdAt = createdAt
)

fun ReadingListItemEntity.toReadingListItem(): ReadingListItem = ReadingListItem(
    id = id,
    listId = listId,
    comicId = comicId,
    position = position
)