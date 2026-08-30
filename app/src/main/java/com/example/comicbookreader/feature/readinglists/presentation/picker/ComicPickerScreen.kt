package com.example.comicbookreader.feature.readinglists.presentation.picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.comicbookreader.R
import com.example.comicbookreader.core.domain.model.Comic
import com.example.comicbookreader.core.presentation.ObserveAsEvents
import com.example.comicbookreader.core.ui.components.ComicRowThumbnail

@Composable
fun ComicPickerRoot(
    listId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ComicPickerViewModel = hiltViewModel()
) {
    LaunchedEffect(listId) { viewModel.load(listId) }

    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ComicPickerEvent.NavigateBack -> onNavigateBack()
        }
    }

    ComicPickerScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicPickerScreen(
    state: ComicPickerState,
    onAction: (ComicPickerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.comic_picker_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(ComicPickerAction.OnBackClick) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { onAction(ComicPickerAction.OnDoneClick) }) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = stringResource(R.string.comic_picker_done)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TextField(
                value = state.searchQuery,
                onValueChange = { onAction(ComicPickerAction.OnSearchQueryChanged(it)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text(stringResource(R.string.comic_picker_search_hint)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            LazyColumn {
                items(state.comics, key = { it.comic.id }) { selectableComic ->
                    SelectableComicRow(
                        comic = selectableComic.comic,
                        isSelected = selectableComic.isSelected,
                        onToggle = { onAction(ComicPickerAction.OnComicToggled(selectableComic.comic.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectableComicRow(
    comic: Comic,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
        ComicRowThumbnail(title = comic.title, thumbnailPath = comic.thumbnailPath)
        Text(
            text = comic.title,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}