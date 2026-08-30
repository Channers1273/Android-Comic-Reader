package com.example.comicbookreader.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "comic")
data class ComicEntity(
    val title: String,
    val pageCount: Int,
    val currentPage: Int = 0,
    val lastOpened: LocalDateTime?,
    val dateAdded: LocalDateTime?,
    val fileUri: String,
    val thumbnailPath: String?,
    val folderPath: String,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)