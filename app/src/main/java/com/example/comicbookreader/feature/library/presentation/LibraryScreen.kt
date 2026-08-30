package com.example.comicbookreader.feature.library.presentation

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.comicbookreader.core.presentation.ObserveAsEvents
import com.example.comicbookreader.core.ui.components.ComicThumbnail
import com.example.comicbookreader.core.ui.components.FolderTile
import kotlinx.coroutines.launch

@Composable
fun LibraryRoot(
    onFolderClick: (String) -> Unit,
    onComicClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { folderUri ->
            folderUri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.onAction(LibraryAction.OnDirectoryPicked(it.toString()))
            }
        }
    )

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            LibraryEvent.RequestDirectoryPicker -> directoryPickerLauncher.launch(null)
            is LibraryEvent.NavigateToFolder -> onFolderClick(event.folderPath)
            is LibraryEvent.NavigateToReader -> onComicClick(event.comicId)
            is LibraryEvent.ShowError -> {
                scope.launch { snackbarHostState.showSnackbar(event.message.asString(context)) }
            }
        }
    }

    LibraryScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    state: LibraryState,
    onAction: (LibraryAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Library") },
                actions = {
                    IconButton(onClick = { onAction(LibraryAction.OnChangeDirectoryClick) }) {
                        Icon(
                            imageVector = Icons.Filled.FolderOpen,
                            contentDescription = if (state.comicDirectory == null) {
                                "Select comic directory"
                            } else {
                                "Change comic directory"
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            state.comicDirectory == null -> {
                CenteredMessage(
                    text = "Choose a folder to build your library",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            state.isSyncing && state.folders.isEmpty() && state.rootComics.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Scanning your comics…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            state.folders.isEmpty() && state.rootComics.isEmpty() -> {
                CenteredMessage(
                    text = "No comics found in this folder",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = state.isSyncing,
                    onRefresh = { onAction(LibraryAction.OnRefresh) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(8.dp),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        items(state.folders, key = { "folder_${it.folderPath}" }) { folder ->
                            FolderTile(
                                name = folder.name,
                                comicCount = folder.comicCount,
                                onClick = { onAction(LibraryAction.OnFolderClick(folder.folderPath)) }
                            )
                        }

                        items(state.rootComics, key = { "comic_${it.id}" }) { comic ->
                            ComicThumbnail(
                                title = comic.title,
                                thumbnailPath = comic.thumbnailPath,
                                progressFraction = if (comic.pageCount > 0) {
                                    comic.currentPage / comic.pageCount.toFloat()
                                } else {
                                    0f
                                },
                                onClick = { onAction(LibraryAction.OnComicClick(comic.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CenteredMessage(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}