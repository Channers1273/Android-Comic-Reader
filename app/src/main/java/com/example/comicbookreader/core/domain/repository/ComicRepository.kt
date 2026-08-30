package com.example.comicbookreader.core.domain.repository

import com.example.comicbookreader.core.domain.error.DataError
import com.example.comicbookreader.core.domain.error.EmptyResult
import com.example.comicbookreader.core.domain.error.Result
import com.example.comicbookreader.core.domain.model.Comic
import kotlinx.coroutines.flow.Flow

interface ComicRepository {
    fun unzipComic(comicId: Int, comicFileUri: String): Flow<Result<String, DataError.Local>>

    suspend fun syncLibrary(rootFolderUri: String): EmptyResult<DataError.Local>

    fun getAllComics(): Flow<List<Comic>>

    suspend fun getComicById(comicId: Int): Comic?

    suspend fun updateReadingProgress(comicId: Int, page: Int): EmptyResult<DataError.Local>
}