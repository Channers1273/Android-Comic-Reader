package com.example.comicbookreader.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.comicbookreader.R
import java.io.File

@Composable
fun ComicRowThumbnail(
    title: String,
    thumbnailPath: String?,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = thumbnailPath?.let(::File),
        contentDescription = stringResource(R.string.cd_comic_cover, title),
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(width = 48.dp, height = 64.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    )
}