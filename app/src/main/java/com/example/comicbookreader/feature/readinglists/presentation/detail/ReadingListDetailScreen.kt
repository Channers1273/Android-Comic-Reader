package com.example.comicbookreader.feature.readinglists.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.comicbookreader.core.presentation.ObserveAsEvents

@Composable
fun ReadingListDetailRoot(
    listId: Long,
    onComicClick: (Int, Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReadingListDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(listId) { viewModel.load(listId) }

    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ReadingListDetailEvent.NavigateToReader -> onComicClick(event.comicId, event.listId)
        }
    }

    ReadingListDetailScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@Composable
fun ReadingListDetailScreen(
    state: ReadingListDetailState,
    onAction: (ReadingListDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            LazyColumn {
                items(state.items, key = { it.itemId }) { item ->
                    ReadingListItemRow(
                        item = item,
                        onClick = { onAction(ReadingListDetailAction.OnItemClick(item.comicId)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadingListItemRow(
    item: ReadingListItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = item.title,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp)
        )
    }
}