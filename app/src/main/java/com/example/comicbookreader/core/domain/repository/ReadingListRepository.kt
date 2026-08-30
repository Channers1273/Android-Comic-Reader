package com.example.comicbookreader.core.domain.repository

import com.example.comicbookreader.core.domain.model.ReadingList
import com.example.comicbookreader.core.domain.model.ReadingListItem
import kotlinx.coroutines.flow.Flow

interface ReadingListRepository {
    fun getAllLists(): Flow<List<ReadingList>>

    fun getItemsForList(listId: Long): Flow<List<ReadingListItem>>

    suspend fun createList(name: String): Long

    suspend fun renameList(listId: Long, name: String)

    suspend fun deleteList(listId: Long)

    suspend fun addComicToList(listId: Long, comicId: Int)

    suspend fun removeItemFromList(itemId: Long)

    suspend fun reorderItems(orderedItemIds: List<Long>)

    suspend fun getNextItemInList(listId: Long, currentComicId: Int): ReadingListItem?
}