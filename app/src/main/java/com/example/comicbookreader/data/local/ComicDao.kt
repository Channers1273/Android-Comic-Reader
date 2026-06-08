package com.example.comicbookreader.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.comicbookreader.data.model.Comic
import kotlinx.coroutines.flow.Flow

@Dao
interface ComicDao {

    @Upsert
    suspend fun upsertComic(comic: Comic)

    @Query("SELECT * FROM comic")
    fun getAllComics(): Flow<List<Comic>>

    @Query("SELECT * FROM comic")
    suspend fun getAllComicsOnce(): List<Comic>

}