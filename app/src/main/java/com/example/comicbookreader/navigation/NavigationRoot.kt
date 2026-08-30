package com.example.comicbookreader.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.example.comicbookreader.feature.collection.presentation.detail.CollectionDetailRoot
import com.example.comicbookreader.feature.library.presentation.LibraryRoot
import com.example.comicbookreader.feature.reader.presentation.ReaderRoot
import com.example.comicbookreader.feature.readinglists.presentation.detail.ReadingListDetailRoot
import com.example.comicbookreader.feature.readinglists.presentation.editcontents.ReadingListEditContentsRoot
import com.example.comicbookreader.feature.readinglists.presentation.lists.ReadingListsRoot
import com.example.comicbookreader.feature.readinglists.presentation.picker.ComicPickerRoot
import com.example.comicbookreader.feature.settings.presentation.SettingsRoot
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private val TOP_LEVEL_ROUTES = setOf<NavKey>(Route.Library, Route.ReadingLists, Route.Settings)

@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Route.Library::class, Route.Library.serializer())
                    subclass(Route.CollectionDetail::class, Route.CollectionDetail.serializer())
                    subclass(Route.ReadingLists::class, Route.ReadingLists.serializer())
                    subclass(Route.ReadingListDetail::class, Route.ReadingListDetail.serializer())
                    subclass(
                        Route.ReadingListEditContents::class,
                        Route.ReadingListEditContents.serializer()
                    )
                    subclass(Route.ComicPicker::class, Route.ComicPicker.serializer())
                    subclass(Route.Reader::class, Route.Reader.serializer())
                    subclass(Route.Settings::class, Route.Settings.serializer())
                }
            }
        },
        Route.Library
    )

    fun switchTab(route: Route) {
        if (backStack.size != 1 || backStack.firstOrNull() != route) {
            backStack.clear()
            backStack.add(route)
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            val currentRoute = backStack.lastOrNull()
            if (currentRoute != null && currentRoute in TOP_LEVEL_ROUTES) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Route.Library,
                        onClick = { switchTab(Route.Library) },
                        icon = { Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = "Library") },
                        label = { Text("Library") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Route.ReadingLists,
                        onClick = { switchTab(Route.ReadingLists) },
                        icon = { Icon(Icons.Filled.Bookmarks, contentDescription = "Reading Lists") },
                        label = { Text("Reading Lists") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Route.Settings,
                        onClick = { switchTab(Route.Settings) },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }
        }
    ) { innerPadding ->
        val currentRoute = backStack.lastOrNull()
        val bottomPadding = if (currentRoute != null && currentRoute in TOP_LEVEL_ROUTES) {
            innerPadding.calculateBottomPadding()
        } else {
            0.dp
        }
        NavDisplay(
            modifier = Modifier.padding(bottom = bottomPadding),
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
                                onFolderClick = { folderPath ->
                                    backStack.add(Route.CollectionDetail(folderPath))
                                },
                                onComicClick = { comicId -> backStack.add(Route.Reader(comicId)) }
                            )
                        }
                    }

                    is Route.CollectionDetail -> {
                        NavEntry(key) {
                            CollectionDetailRoot(
                                folderPath = key.folderPath,
                                onComicClick = { comicId -> backStack.add(Route.Reader(comicId)) },
                                onBackClick = { backStack.removeLastOrNull() }
                            )
                        }
                    }

                    is Route.ReadingLists -> {
                        NavEntry(key) {
                            ReadingListsRoot(
                                onListClick = { listId -> backStack.add(Route.ReadingListDetail(listId)) },
                                onEditContentsClick = { listId ->
                                    backStack.add(Route.ReadingListEditContents(listId))
                                },
                                onNavigateToPicker = { listId -> backStack.add(Route.ComicPicker(listId)) }
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

                    is Route.ReadingListEditContents -> {
                        NavEntry(key) {
                            ReadingListEditContentsRoot(
                                listId = key.listId,
                                onAddComicsClick = { listId -> backStack.add(Route.ComicPicker(listId)) },
                                onBackClick = { backStack.removeLastOrNull() }
                            )
                        }
                    }

                    is Route.ComicPicker -> {
                        NavEntry(key) {
                            ComicPickerRoot(
                                listId = key.listId,
                                onNavigateBack = { backStack.removeLastOrNull() }
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
                                },
                                onNavigateBack = { backStack.removeLastOrNull() }
                            )
                        }
                    }

                    is Route.Settings -> {
                        NavEntry(key) {
                            SettingsRoot()
                        }
                    }

                    else -> error("Unknown NavKey: $key")
                }
            }
        )
    }
}