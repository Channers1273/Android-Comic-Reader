package com.example.comicbookreader.core.data.repository

import com.example.comicbookreader.core.data.mapper.toReadingList
import com.example.comicbookreader.core.data.mapper.toReadingListItem
import com.example.comicbookreader.core.database.dao.ReadingListDao
import com.example.comicbookreader.core.database.entity.ReadingListEntity
import com.example.comicbookreader.core.database.entity.ReadingListItemEntity
import com.example.comicbookreader.core.domain.model.ReadingList
import com.example.comicbookreader.core.domain.model.ReadingListItem
import com.example.comicbookreader.core.domain.repository.ReadingListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class DefaultReadingListRepository @Inject constructor(
    private val readingListDao: ReadingListDao
) : ReadingListRepository {

    override fun getAllLists(): Flow<List<ReadingList>> {
        return readingListDao.getAllLists().map { lists -> lists.map { it.toReadingList() } }
    }

    override fun getItemsForList(listId: Long): Flow<List<ReadingListItem>> {
        return readingListDao.getItemsForList(listId).map { items -> items.map { it.toReadingListItem() } }
    }

    override suspend fun createList(name: String): Long {
        return readingListDao.insertList(ReadingListEntity(name = name, createdAt = LocalDateTime.now()))
    }

    override suspend fun renameList(listId: Long, name: String) {
        readingListDao.renameList(listId, name)
    }

    override suspend fun deleteList(listId: Long) {
        readingListDao.deleteList(listId)
    }

    override suspend fun addComicToList(listId: Long, comicId: Int) {
        val position = readingListDao.getNextPosition(listId)
        readingListDao.insertItem(
            ReadingListItemEntity(listId = listId, comicId = comicId, position = position)
        )
    }

    override suspend fun removeItemFromList(itemId: Long) {
        readingListDao.deleteItem(itemId)
    }

    override suspend fun reorderItems(orderedItemIds: List<Long>) {
        readingListDao.reorderItems(orderedItemIds)
    }

    override suspend fun getNextItemInList(listId: Long, currentComicId: Int): ReadingListItem? {
        return readingListDao.getNextItem(listId, currentComicId)?.toReadingListItem()
    }
}