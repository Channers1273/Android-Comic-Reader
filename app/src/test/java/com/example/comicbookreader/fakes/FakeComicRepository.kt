package com.example.comicbookreader.fakes

import com.example.comicbookreader.core.domain.error.DataError
import com.example.comicbookreader.core.domain.error.EmptyResult
import com.example.comicbookreader.core.domain.error.Result
import com.example.comicbookreader.core.domain.model.Comic
import com.example.comicbookreader.core.domain.repository.ComicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.update

class FakeComicRepository : ComicRepository {

    private val comics = MutableStateFlow<List<Comic>>(emptyList())

    var unzipResults: List<Result<String, DataError.Local>> = emptyList()
    val progressUpdates = mutableListOf<Pair<Int, Int>>()
    val syncLibraryCalls = mutableListOf<String>()
    var syncLibraryResult: EmptyResult<DataError.Local> = Result.Success(Unit)

    fun seedComic(comic: Comic) {
        comics.update { it + comic }
    }

    override fun unzipComic(comicId: Int, comicFileUri: String): Flow<Result<String, DataError.Local>> {
        return unzipResults.asFlow()
    }

    override suspend fun syncLibrary(rootFolderUri: String): EmptyResult<DataError.Local> {
        syncLibraryCalls += rootFolderUri
        return syncLibraryResult
    }

    override fun getAllComics(): Flow<List<Comic>> = comics

    override suspend fun getComicById(comicId: Int): Comic? {
        return comics.value.find { it.id == comicId }
    }

    override suspend fun updateReadingProgress(comicId: Int, page: Int): EmptyResult<DataError.Local> {
        progressUpdates += comicId to page
        return Result.Success(Unit)
    }
}