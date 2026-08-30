package com.example.comicbookreader.core.data.source

import com.example.comicbookreader.core.domain.error.DataError
import com.example.comicbookreader.core.domain.error.Result
import kotlinx.coroutines.flow.Flow

data class ScannedComicFile(
    val fileUri: String,
    val folderPath: String
)

data class ComicFileMetadata(
    val title: String,
    val pageCount: Int,
    val thumbnailPath: String?
)

interface ComicFileDataSource {
    suspend fun scanForComicFiles(rootFolderUri: String): Result<List<ScannedComicFile>, DataError.Local>
    suspend fun readMetadata(fileUri: String): Result<ComicFileMetadata, DataError.Local>
    fun unzipPages(comicId: Int, comicFileUri: String): Flow<Result<String, DataError.Local>>
}