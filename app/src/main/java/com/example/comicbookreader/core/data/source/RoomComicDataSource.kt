package com.example.comicbookreader.core.data.source

import com.example.comicbookreader.core.database.dao.ComicDao
import com.example.comicbookreader.core.database.entity.ComicEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoomComicDataSource @Inject constructor(
    private val comicDao: ComicDao
) : ComicLocalDataSource {

    override suspend fun upsertComic(comic: ComicEntity) = comicDao.upsertComic(comic)

    override fun getAllComics(): Flow<List<ComicEntity>> = comicDao.getAllComics()

    override suspend fun getAllComicsOnce(): List<ComicEntity> = comicDao.getAllComicsOnce()

    override suspend fun getComicById(comicId: Int): ComicEntity? = comicDao.getComicById(comicId)

    override suspend fun updateCurrentPage(comicId: Int, page: Int) =
        comicDao.updateCurrentPage(comicId, page)
}