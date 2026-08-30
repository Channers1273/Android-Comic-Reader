package com.example.comicbookreader.feature.readinglists.presentation.editcontents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.comicbookreader.R
import com.example.comicbookreader.core.presentation.ObserveAsEvents
import com.example.comicbookreader.core.ui.components.ComicRowThumbnail
import com.example.comicbookreader.feature.readinglists.presentation.detail.ReadingListItemUi
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun ReadingListEditContentsRoot(
    listId: Long,
    onAddComicsClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReadingListEditContentsViewModel = hiltViewModel()
) {
    LaunchedEffect(listId) { viewModel.load(listId) }

    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ReadingListEditContentsEvent.NavigateToComicPicker -> onAddComicsClick(event.listId)
            ReadingListEditContentsEvent.NavigateBack -> onBackClick()
        }
    }

    ReadingListEditContentsScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingListEditContentsScreen(
    state: ReadingListEditContentsState,
    onAction: (ReadingListEditContentsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var localItems by remember { mutableStateOf(state.items) }
    LaunchedEffect(state.items) { localItems = state.items }

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        localItems = localItems.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reading_list_edit_contents_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(ReadingListEditContentsAction.OnBackClick) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { onAction(ReadingListEditContentsAction.OnAddComicsClick) }) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = stringResource(R.string.reading_list_edit_contents_add_comics)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.padding(innerPadding)
        ) {
            items(localItems, key = { it.itemId }) { item ->
                ReorderableItem(reorderableState, key = item.itemId) {
                    val dragHandleModifier = Modifier.longPressDraggableHandle(
                        onDragStopped = {
                            onAction(
                                ReadingListEditContentsAction.OnItemsReordered(
                                    localItems.map { it.itemId }
                                )
                            )
                        }
                    )
                    EditableReadingListItemRow(
                        item = item,
                        onRemove = {
                            onAction(ReadingListEditContentsAction.OnRemoveItemClick(item.itemId))
                        },
                        dragHandleModifier = dragHandleModifier
                    )
                }
            }
        }
    }
}

@Composable
private fun EditableReadingListItemRow(
    item: ReadingListItemUi,
    onRemove: () -> Unit,
    dragHandleModifier: Modifier,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Filled.DragHandle,
            contentDescription = stringResource(R.string.reading_list_edit_contents_cd_drag_handle),
            modifier = dragHandleModifier
        )
        ComicRowThumbnail(title = item.title, thumbnailPath = item.thumbnailPath)
        Text(
            text = item.title,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = stringResource(R.string.reading_list_edit_contents_cd_remove)
            )
        }
    }
}