package com.example.comicbookreader.core.data.source

import com.example.comicbookreader.core.database.entity.ComicEntity
import kotlinx.coroutines.flow.Flow

interface ComicLocalDataSource {
    suspend fun upsertComic(comic: ComicEntity)
    fun getAllComics(): Flow<List<ComicEntity>>
    suspend fun getAllComicsOnce(): List<ComicEntity>
    suspend fun getComicById(comicId: Int): ComicEntity?
    suspend fun updateCurrentPage(comicId: Int, page: Int)
}