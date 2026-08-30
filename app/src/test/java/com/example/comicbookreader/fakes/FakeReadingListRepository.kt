package com.example.comicbookreader.fakes

import com.example.comicbookreader.core.domain.model.ReadingList
import com.example.comicbookreader.core.domain.model.ReadingListItem
import com.example.comicbookreader.core.domain.repository.ReadingListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime

class FakeReadingListRepository : ReadingListRepository {

    private val lists = MutableStateFlow<List<ReadingList>>(emptyList())
    private val items = MutableStateFlow<List<ReadingListItem>>(emptyList())
    private var nextItemId = 1L

    fun seedList(list: ReadingList) {
        lists.update { it + list }
    }

    fun seedItem(item: ReadingListItem) {
        items.update { it + item }
        if (item.id >= nextItemId) nextItemId = item.id + 1
    }

    override fun getAllLists(): Flow<List<ReadingList>> = lists

    override fun getItemsForList(listId: Long): Flow<List<ReadingListItem>> {
        return items.map { all -> all.filter { it.listId == listId }.sortedBy { it.position } }
    }

    override suspend fun createList(name: String): Long {
        val id = (lists.value.maxOfOrNull { it.id } ?: 0L) + 1L
        lists.update { it + ReadingList(id, name, LocalDateTime.now()) }
        return id
    }

    override suspend fun renameList(listId: Long, name: String) {
        lists.update { all -> all.map { if (it.id == listId) it.copy(name = name) else it } }
    }

    override suspend fun deleteList(listId: Long) {
        lists.update { all -> all.filterNot { it.id == listId } }
        items.update { all -> all.filterNot { it.listId == listId } }
    }

    override suspend fun addComicToList(listId: Long, comicId: Int) {
        val position = items.value.count { it.listId == listId }
        items.update { it + ReadingListItem(nextItemId++, listId, comicId, position) }
    }

    override suspend fun removeItemFromList(itemId: Long) {
        items.update { all -> all.filterNot { it.id == itemId } }
    }

    override suspend fun reorderItems(orderedItemIds: List<Long>) {
        items.update { all ->
            all.map { item ->
                val newPosition = orderedItemIds.indexOf(item.id)
                if (newPosition >= 0) item.copy(position = newPosition) else item
            }
        }
    }

    override suspend fun getNextItemInList(listId: Long, currentComicId: Int): ReadingListItem? {
        val listItems = items.value.filter { it.listId == listId }.sortedBy { it.position }
        val currentIndex = listItems.indexOfFirst { it.comicId == currentComicId }
        if (currentIndex == -1 || currentIndex + 1 >= listItems.size) return null
        return listItems[currentIndex + 1]
    }
}