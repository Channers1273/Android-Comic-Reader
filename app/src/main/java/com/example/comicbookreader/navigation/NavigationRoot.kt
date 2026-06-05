package com.example.comicbookreader.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.example.comicbookreader.screens.HomeScreen
import com.example.comicbookreader.screens.ReaderScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Route.Home::class, Route.Home.serializer())
                    subclass(Route.Reader::class, Route.Reader.serializer())
                }
            }
        },
        Route.Home
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
                is Route.Home -> {
                    NavEntry(key) {

                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.OpenDocument(),
                            onResult = { contentUri ->
                                backStack.add(Route.Reader(contentUri.toString()))
                            }
                        )
                        HomeScreen(
                            onSelectComicClick = {
                                launcher.launch(arrayOf("application/x-cbz"))
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
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