package com.example.comicbookreader.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.comicbookreader.data.model.Comic
import com.example.comicbookreader.viewmodels.CollectionViewModel

@Composable
fun CollectionScreen(
    onThumbnailClick: (Uri?) -> Unit,
    modifier: Modifier = Modifier,
    collectionViewModel: CollectionViewModel = hiltViewModel()
) {

    val comics by collectionViewModel.comics.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = innerPadding,
            modifier = Modifier.padding(8.dp)
        ) {
            items(comics) { comic ->
                ComicThumbnail(
                    comic,
                    onThumbnailClick = { onThumbnailClick(comic.fileUri) }
                )
            }
        }
    }
}

@Composable
fun ComicThumbnail(
    comic: Comic,
    onThumbnailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box (
        modifier = modifier
            .fillMaxSize()
            .border (
                width = 1.dp,
                color = Color.Blue,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                onClick = onThumbnailClick
            )
    ) {
        Column() {
            AsyncImage(
                comic.thumbnailPath,
                contentDescription = null,
                modifier = Modifier
            )

            Row(
                modifier = Modifier
                    //.align(Alignment.BottomCenter)
                    .background(Color.Black)
            ) {
                Text(comic.title)

                // OptionsButton()
            }
        }
    }
}
