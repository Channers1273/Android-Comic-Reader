package com.example.comicbookreader.core.domain.error

sealed interface DataError : Error {
    enum class Local : DataError {
        NOT_FOUND,
        DISK_FULL,
        PERMISSION_DENIED,
        CORRUPTED_ARCHIVE,
        UNKNOWN
    }
}