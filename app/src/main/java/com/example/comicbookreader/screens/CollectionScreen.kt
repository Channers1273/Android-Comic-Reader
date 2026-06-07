package com.example.comicbookreader.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.comicbookreader.viewmodels.CollectionViewModel

@Composable
fun CollectionScreen(
    modifier: Modifier = Modifier,
    collectionViewModel: CollectionViewModel = hiltViewModel()
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
//        LazyColumn(modifier = Modifier.padding(innerPadding)) {
//            items()
//        }
        Text("Collection Screen", modifier = Modifier.padding(innerPadding))

    }
}

@Composable
fun ComicThumbnail(title: String, modifier: Modifier = Modifier) {
    Text(title)
}