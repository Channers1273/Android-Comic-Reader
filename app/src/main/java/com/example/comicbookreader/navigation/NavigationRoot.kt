package com.example.comicbookreader.navigation

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.example.comicbookreader.screens.CollectionScreen
import com.example.comicbookreader.screens.LibraryScreen
import com.example.comicbookreader.screens.ReaderScreen
import com.example.comicbookreader.viewmodels.UserSettingsViewModel
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Route.Library::class, Route.Library.serializer())
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
            when(key) {
                is Route.Library -> {
                    NavEntry(key) {

                        val userSettingsViewModel = hiltViewModel<UserSettingsViewModel>()

                        val context = LocalContext.current

                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.OpenDocumentTree(),
                            onResult = { folderUri ->
                                folderUri?.let {
                                    context.contentResolver.takePersistableUriPermission(
                                        it,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    )
                                    userSettingsViewModel.onComicDirectoryChanged(it.toString())
                                }
                            }
                        )
                        LibraryScreen(
                            onSelectComicDirectoryClick = {
                                launcher.launch(null)
                            },
                            onGoToCollectionClick = {
                                backStack.add(Route.Collection)
                            }
                        )
                    }
                }

                is Route.Collection -> {
                    NavEntry(key) {
                        CollectionScreen()
                    }
                }

                is Route.Reader -> {
                    NavEntry(key) {
                        ReaderScreen(
                            comicUri = key.comicUri
                        )
                    }
                }

                else -> error("Unknown Navkey: $key")
            }
        }
    )
}