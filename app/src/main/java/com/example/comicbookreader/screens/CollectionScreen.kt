package com.example.comicbookreader.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.comicbookreader.data.model.Comic
import com.example.comicbookreader.viewmodels.CollectionViewModel

@Composable
fun CollectionScreen(
    modifier: Modifier = Modifier,
    collectionViewModel: CollectionViewModel = hiltViewModel()
) {

    val comics by collectionViewModel.comics.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.padding(innerPadding)) {
            items(comics) { comic ->
                ComicThumbnail(comic)
            }
        }
    }
}

@Composable
fun ComicThumbnail(comic: Comic, modifier: Modifier = Modifier) {
    AsyncImage(
        comic.thumbnailPath,
        contentDescription = null,
        modifier = modifier
    )
}
