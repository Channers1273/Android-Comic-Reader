package com.example.comicbookreader.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.comicbookreader.core.database.entity.ReadingListEntity
import com.example.comicbookreader.core.database.entity.ReadingListItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingListDao {

    @Insert
    suspend fun insertList(list: ReadingListEntity): Long

    @Query("UPDATE reading_list SET name = :name WHERE id = :listId")
    suspend fun renameList(listId: Long, name: String)

    @Query("DELETE FROM reading_list WHERE id = :listId")
    suspend fun deleteList(listId: Long)

    @Query("SELECT * FROM reading_list ORDER BY createdAt DESC")
    fun getAllLists(): Flow<List<ReadingListEntity>>

    @Query("SELECT * FROM reading_list_item WHERE listId = :listId ORDER BY position ASC")
    fun getItemsForList(listId: Long): Flow<List<ReadingListItemEntity>>

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM reading_list_item WHERE listId = :listId")
    suspend fun getNextPosition(listId: Long): Int

    @Insert
    suspend fun insertItem(item: ReadingListItemEntity)

    @Query("DELETE FROM reading_list_item WHERE id = :itemId")
    suspend fun deleteItem(itemId: Long)

    @Query("UPDATE reading_list_item SET position = :position WHERE id = :itemId")
    suspend fun updatePosition(itemId: Long, position: Int)

    @Transaction
    suspend fun reorderItems(orderedItemIds: List<Long>) {
        orderedItemIds.forEachIndexed { index, itemId -> updatePosition(itemId, index) }
    }

    @Query(
        """
        SELECT * FROM reading_list_item
        WHERE listId = :listId AND position = (
            SELECT position FROM reading_list_item WHERE listId = :listId AND comicId = :currentComicId
        ) + 1
        """
    )
    suspend fun getNextItem(listId: Long, currentComicId: Int): ReadingListItemEntity?
}