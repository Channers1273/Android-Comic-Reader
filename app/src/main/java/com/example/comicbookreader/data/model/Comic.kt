package com.example.comicbookreader.data.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime


@Entity
data class Comic (
    val title: String,
    val pageCount: Int,
    val currentPage: Int = 0,
    val lastOpened: LocalDateTime?,
    val dateAdded: LocalDateTime?,
    val fileUri: Uri?,
    val thumbnailPath: String?,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)