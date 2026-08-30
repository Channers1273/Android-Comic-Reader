package com.example.comicbookreader.feature.reader.presentation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.comicbookreader.core.presentation.ObserveAsEvents
import com.example.comicbookreader.feature.reader.presentation.components.ReaderBottomBar
import com.example.comicbookreader.feature.reader.presentation.components.ReaderTopBar
import java.io.File
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage

@Composable
fun ReaderRoot(
    comicId: Int,
    readingListId: Long?,
    onNavigateToNextComic: (comicId: Int, listId: Long) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    LaunchedEffect(comicId, readingListId) { viewModel.loadComic(comicId, readingListId) }

    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ReaderEvent.NavigateToNextComic -> onNavigateToNextComic(event.comicId, event.listId)
        }
    }

    ReaderScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@Composable
fun ReaderScreen(
    state: ReaderState,
    onAction: (ReaderAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listContext = state.listContext
    val hasNextComicPage = listContext is ReadingListContext.InList && listContext.nextComic != null
    val pageCount = state.pages.size + if (hasNextComicPage) 1 else 0
    val pagerState = rememberPagerState(pageCount = { pageCount })
    var hasRestoredPosition by rememberSaveable(state.comicId) { mutableStateOf(false) }

    LaunchedEffect(state.pages.size) {
        if (!hasRestoredPosition && state.pages.size > state.currentPageIndex) {
            if (state.currentPageIndex > 0) {
                pagerState.scrollToPage(state.currentPageIndex)
            }
            hasRestoredPosition = true
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (hasRestoredPosition && state.pages.isNotEmpty() && pagerState.currentPage < state.pages.size) {
            onAction(ReaderAction.OnPageChanged(pagerState.currentPage))
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.error != null -> {
                    Text(
                        text = state.error.asString(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    HorizontalPager(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),
                        state = pagerState
                    ) { page ->
                        if (page < state.pages.size) {
                            ZoomableAsyncImage(
                                model = File(state.pages[page]),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                onClick = { onAction(ReaderAction.OnToggleOverlay) }
                            )
                        } else if (listContext is ReadingListContext.InList && listContext.nextComic != null) {
                            NextComicTransitionPage(
                                title = listContext.nextComic.title,
                                thumbnailPath = listContext.nextComic.thumbnailPath,
                                onContinueClick = { onAction(ReaderAction.OnContinueToNextComic) },
                                onToggleOverlay = { onAction(ReaderAction.OnToggleOverlay) }
                            )
                        }
                    }
                }
            }

            if (state.isOverlayVisible) {
                ReaderTopBar(
                    title = state.title,
                    onBackClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .height(90.dp)
                        .fillMaxWidth()
                )
            }

            if (state.isOverlayVisible) {
                ReaderBottomBar(
                    pageNumber = pagerState.currentPage,
                    totalPages = state.pageCount,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(90.dp)
                )
            }
        }
    }
}

@Composable
private fun NextComicTransitionPage(
    title: String,
    thumbnailPath: String?,
    onContinueClick: () -> Unit,
    onToggleOverlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .pointerInput(Unit) {
                detectTapGestures {
                    onToggleOverlay()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AsyncImage(
            model = thumbnailPath?.let(::File),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
        )
        Text(
            text = "Up next: $title",
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
        )
        Button(onClick = onContinueClick) {
            Text("Continue")
        }
    }
}