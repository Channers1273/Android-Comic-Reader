package com.example.comicbookreader.screens

import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.comicbookreader.viewmodels.ReaderViewModel

@Composable
fun ReaderScreen(
    comicUri: Uri,
    modifier: Modifier = Modifier,
    viewModel: ReaderViewModel = hiltViewModel()
) {

    Log.d("Tag", "Entered Reader Screen")

    // makes it so that this is only ran once on initial composition, then only ran in
    // recompositions if this value changed
    LaunchedEffect(comicUri) {
        viewModel.loadComic(comicUri)
    }

    val pages by viewModel.pages.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(pageCount = {
        pages.size
    })

    var isOverlayVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            isOverlayVisible = !isOverlayVisible
                        }

                    }
                    .align(Alignment.Center),
                state = pagerState
            ) { page ->

                AsyncImage(
                    model = pages[page],
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                )

            }

            AnimatedVisibility(
                isOverlayVisible,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)

            ) {
                TopBar(
                    modifier = Modifier
                        .height(90.dp)
                        .fillMaxWidth()
                        .background(Color.Red)
                )
            }

            AnimatedVisibility(
                isOverlayVisible,
                enter = slideInVertically { it }, // The 'it' is the full height of the composable, so it slides in from exactly its own height below the screen.
                exit = slideOutVertically { it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {
                BottomBar(
                    pageNumber = pagerState.currentPage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .background(Color.Green)
                )
            }
        }
    }
}

@Composable
fun TopBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Test Top Bar")
    }
}

@Composable
fun BottomBar(
    pageNumber: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Page: $pageNumber")
    }
}