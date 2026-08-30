package com.example.comicbookreader.feature.readinglists.presentation.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.comicbookreader.R
import com.example.comicbookreader.core.domain.model.ReadingList
import com.example.comicbookreader.core.presentation.ObserveAsEvents
import com.example.comicbookreader.core.ui.components.TextInputDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

@Composable
fun ReadingListsRoot(
    onListClick: (Long) -> Unit,
    onEditContentsClick: (Long) -> Unit,
    onNavigateToPicker: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReadingListsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ReadingListsEvent.NavigateToListDetail -> onListClick(event.listId)
            is ReadingListsEvent.NavigateToEditContents -> onEditContentsClick(event.listId)
            is ReadingListsEvent.NavigateToComicPicker -> onNavigateToPicker(event.listId)
        }
    }

    ReadingListsScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingListsScreen(
    state: ReadingListsState,
    onAction: (ReadingListsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reading_lists_title)) },
                actions = {
                    IconButton(onClick = { onAction(ReadingListsAction.OnAddListClick) }) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = stringResource(R.string.reading_lists_cd_create)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.lists.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.reading_lists_empty_state),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 8.dp)
            ) {
                items(state.lists, key = { it.id }) { list ->
                    ReadingListRow(
                        list = list,
                        isMenuOpen = state.menuOpenForListId == list.id,
                        onClick = { onAction(ReadingListsAction.OnListClick(list.id)) },
                        onMenuClick = { onAction(ReadingListsAction.OnMenuClick(list.id)) },
                        onMenuDismiss = { onAction(ReadingListsAction.OnMenuDismiss) },
                        onRenameClick = { onAction(ReadingListsAction.OnRenameMenuItemClick(list)) },
                        onEditContentsClick = {
                            onAction(ReadingListsAction.OnEditContentsMenuItemClick(list.id))
                        },
                        onDeleteClick = { onAction(ReadingListsAction.OnDeleteMenuItemClick(list)) }
                    )
                }
            }
        }
    }

    if (state.showCreateDialog) {
        TextInputDialog(
            title = stringResource(R.string.reading_lists_create_dialog_title),
            initialValue = "",
            confirmLabel = stringResource(R.string.reading_lists_create_confirm),
            onConfirm = { onAction(ReadingListsAction.OnCreateListConfirm(it)) },
            onDismiss = { onAction(ReadingListsAction.OnCreateDialogDismiss) }
        )
    }

    state.listPendingRename?.let { list ->
        TextInputDialog(
            title = stringResource(R.string.reading_lists_rename_dialog_title),
            initialValue = list.name,
            confirmLabel = stringResource(R.string.reading_lists_rename_confirm),
            onConfirm = { onAction(ReadingListsAction.OnRenameConfirm(list.id, it)) },
            onDismiss = { onAction(ReadingListsAction.OnRenameDialogDismiss) }
        )
    }

    state.listPendingDelete?.let { list ->
        AlertDialog(
            onDismissRequest = { onAction(ReadingListsAction.OnDeleteDialogDismiss) },
            title = {
                Text(stringResource(R.string.reading_lists_delete_confirm_title, list.name))
            },
            confirmButton = {
                TextButton(onClick = { onAction(ReadingListsAction.OnDeleteConfirm(list.id)) }) {
                    Text(
                        stringResource(R.string.reading_lists_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ReadingListsAction.OnDeleteDialogDismiss) }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

@Composable
private fun ReadingListRow(
    list: ReadingList,
    isMenuOpen: Boolean,
    onClick: () -> Unit,
    onMenuClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    onRenameClick: () -> Unit,
    onEditContentsClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = list.name,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(end = 48.dp, top = 12.dp, bottom = 12.dp)
        )

        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.reading_lists_cd_menu)
                )
            }
            DropdownMenu(expanded = isMenuOpen, onDismissRequest = onMenuDismiss) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.reading_lists_rename)) },
                    onClick = onRenameClick
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.reading_lists_edit_contents)) },
                    onClick = onEditContentsClick
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(R.string.reading_lists_delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = onDeleteClick
                )
            }
        }
    }
}