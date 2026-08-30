package com.example.comicbookreader.core.data.repository

import com.example.comicbookreader.core.data.mapper.toComic
import com.example.comicbookreader.core.data.mapper.toComicEntity
import com.example.comicbookreader.core.data.source.ComicFileDataSource
import com.example.comicbookreader.core.data.source.ComicLocalDataSource
import com.example.comicbookreader.core.data.source.ScannedComicFile
import com.example.comicbookreader.core.domain.error.DataError
import com.example.comicbookreader.core.domain.error.EmptyResult
import com.example.comicbookreader.core.domain.error.Result
import com.example.comicbookreader.core.domain.error.onFailure
import com.example.comicbookreader.core.domain.error.onSuccess
import com.example.comicbookreader.core.domain.model.Comic
import com.example.comicbookreader.core.domain.repository.ComicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class DefaultComicRepository @Inject constructor(
    private val fileDataSource: ComicFileDataSource,
    private val localDataSource: ComicLocalDataSource
) : ComicRepository {

    override fun unzipComic(comicId: Int, comicFileUri: String): Flow<Result<String, DataError.Local>> {
        return fileDataSource.unzipPages(comicId, comicFileUri)
    }

    override suspend fun syncLibrary(rootFolderUri: String): EmptyResult<DataError.Local> {
        var outcome: EmptyResult<DataError.Local> = Result.Success(Unit)

        fileDataSource.scanForComicFiles(rootFolderUri)
            .onSuccess { scannedFiles -> upsertNewComics(scannedFiles) }
            .onFailure { error -> outcome = Result.Error(error) }

        return outcome
    }

    private suspend fun upsertNewComics(scannedFiles: List<ScannedComicFile>) {
        val existingUris = localDataSource.getAllComicsOnce().map { it.fileUri }
        val newFiles = scannedFiles.filter { it.fileUri !in existingUris }

        newFiles.forEach { scanned ->
            fileDataSource.readMetadata(scanned.fileUri).onSuccess { metadata ->
                val comic = Comic(
                    title = metadata.title,
                    pageCount = metadata.pageCount,
                    lastOpened = null,
                    dateAdded = LocalDateTime.now(),
                    fileUri = scanned.fileUri,
                    thumbnailPath = metadata.thumbnailPath,
                    folderPath = scanned.folderPath
                )
                localDataSource.upsertComic(comic.toComicEntity())
            }
        }
    }

    override fun getAllComics(): Flow<List<Comic>> {
        return localDataSource.getAllComics().map { entities -> entities.map { it.toComic() } }
    }

    override suspend fun getComicById(comicId: Int): Comic? {
        return localDataSource.getComicById(comicId)?.toComic()
    }

    override suspend fun updateReadingProgress(comicId: Int, page: Int): EmptyResult<DataError.Local> {
        localDataSource.updateCurrentPage(comicId, page)
        return Result.Success(Unit)
    }
}