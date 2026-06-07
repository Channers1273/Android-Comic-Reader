package com.example.comicbookreader.data.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class Comic (
    val title: String,
    val pageCount: Int,
    val currentPage: Int = 0,
    val lastOpened: String?,
    val dateAdded: String,
    val fileUriString: String,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)


