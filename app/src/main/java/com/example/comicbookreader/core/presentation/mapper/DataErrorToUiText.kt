package com.example.comicbookreader.core.presentation.mapper

import com.example.comicbookreader.R
import com.example.comicbookreader.core.domain.error.DataError
import com.example.comicbookreader.core.presentation.UiText

fun DataError.Local.toUiText(): UiText {
    val resId = when (this) {
        DataError.Local.NOT_FOUND -> R.string.error_comic_not_found
        DataError.Local.DISK_FULL -> R.string.error_disk_full
        DataError.Local.PERMISSION_DENIED -> R.string.error_permission_denied
        DataError.Local.CORRUPTED_ARCHIVE -> R.string.error_corrupted_archive
        DataError.Local.UNKNOWN -> R.string.error_unknown
    }
    return UiText.StringResource(resId)
}