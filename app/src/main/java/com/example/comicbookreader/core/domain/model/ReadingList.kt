package com.example.comicbookreader.core.domain.model

import java.time.LocalDateTime

data class ReadingList(
    val id: Long = 0,
    val name: String,
    val createdAt: LocalDateTime
)