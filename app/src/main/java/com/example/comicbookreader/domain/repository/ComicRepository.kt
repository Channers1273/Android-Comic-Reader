package com.example.comicbookreader.domain.repository

import android.content.Context
import android.net.Uri
import com.example.comicbookreader.data.model.Comic
import kotlinx.coroutines.flow.Flow

interface ComicRepository {
    fun unzipComic(
        comicUri: Uri
    ): Flow<Uri>

    suspend fun syncLibrary(folderUri: Uri)

    fun getAllComics(): Flow<List<Comic>>
}