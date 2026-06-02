package com.example.comicbookreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.comicbookreader.navigation.NavigationRoot
import com.example.comicbookreader.ui.theme.ComicBookReaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ComicBookReaderTheme {
                NavigationRoot()
            }
        }
    }
}