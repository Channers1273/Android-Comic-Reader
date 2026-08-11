package com.example.comicbookreader.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.example.comicbookreader.feature.collection.presentation.detail.CollectionDetailRoot
import com.example.comicbookreader.feature.collection.presentation.list.CollectionListRoot
import com.example.comicbookreader.feature.library.presentation.LibraryRoot
import com.example.comicbookreader.feature.reader.presentation.ReaderRoot
import com.example.comicbookreader.feature.readinglists.presentation.detail.ReadingListDetailRoot
import com.example.comicbookreader.feature.readinglists.presentation.lists.ReadingListsRoot
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Route.Library::class, Route.Library.serializer())
                    subclass(Route.Collections::class, Route.Collections.serializer())
                    subclass(Route.CollectionDetail::class, Route.CollectionDetail.serializer())
                    subclass(Route.ReadingLists::class, Route.ReadingLists.serializer())
                    subclass(Route.ReadingListDetail::class, Route.ReadingListDetail.serializer())
                    subclass(Route.Reader::class, Route.Reader.serializer())
                }
            }
        },
        Route.Library
    )

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = { key ->
            when (key) {
                is Route.Library -> {
                    NavEntry(key) {
                        LibraryRoot(
                            onNavigateToCollections = { backStack.add(Route.Collections) },
                            onNavigateToReadingLists = { backStack.add(Route.ReadingLists) }
                        )
                    }
                }

                is Route.Collections -> {
                    NavEntry(key) {
                        CollectionListRoot(
                            onCollectionClick = { folderPath ->
                                backStack.add(Route.CollectionDetail(folderPath))
                            }
                        )
                    }
                }

                is Route.CollectionDetail -> {
                    NavEntry(key) {
                        CollectionDetailRoot(
                            folderPath = key.folderPath,
                            onComicClick = { comicId -> backStack.add(Route.Reader(comicId)) }
                        )
                    }
                }

                is Route.ReadingLists -> {
                    NavEntry(key) {
                        ReadingListsRoot(
                            onListClick = { listId -> backStack.add(Route.ReadingListDetail(listId)) }
                        )
                    }
                }

                is Route.ReadingListDetail -> {
                    NavEntry(key) {
                        ReadingListDetailRoot(
                            listId = key.listId,
                            onComicClick = { comicId, listId ->
                                backStack.add(Route.Reader(comicId, listId))
                            }
                        )
                    }
                }

                is Route.Reader -> {
                    NavEntry(key) {
                        ReaderRoot(
                            comicId = key.comicId,
                            readingListId = key.readingListId,
                            onNavigateToNextComic = { comicId, listId ->
                                backStack.removeLastOrNull()
                                backStack.add(Route.Reader(comicId, listId))
                            }
                        )
                    }
                }

                else -> error("Unknown NavKey: $key")
            }
        }
    )
}