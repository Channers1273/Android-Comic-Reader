package com.example.comicbookreader.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.comicbookreader.core.database.entity.ComicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ComicDao {

    @Upsert
    suspend fun upsertComic(comic: ComicEntity)

    @Query("SELECT * FROM comic")
    fun getAllComics(): Flow<List<ComicEntity>>

    @Query("SELECT * FROM comic")
    suspend fun getAllComicsOnce(): List<ComicEntity>

    @Query("SELECT * FROM comic WHERE id = :comicId")
    suspend fun getComicById(comicId: Int): ComicEntity?

    @Query("UPDATE comic SET currentPage = :page WHERE id = :comicId")
    suspend fun updateCurrentPage(comicId: Int, page: Int)
}