package com.example.comicbookreader.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun ReaderScreen(
    modifier: Modifier = Modifier
) {
    Log.d("Tag", "Entered Reader Screen")

    val imgUrl = "https://www.bu.edu/files/2024/08/Hey-BU-Blog-Headers.jpg"


   val pagerState = rememberPagerState(pageCount = {
       10
   })
   HorizontalPager(
       modifier = Modifier.fillMaxWidth(),
       state = pagerState
   ) { page ->
       Box(
           modifier = Modifier.fillMaxSize()
       ) {
           AsyncImage (
               model = imgUrl,
               contentDescription = null,
               modifier = Modifier
                   .fillMaxSize()
                   .aspectRatio(2000f / 1308f)
           )
           Text(
               "Page: $page",
               modifier = Modifier
                   .align(Alignment.BottomCenter)
                   .padding(16.dp)
               )
       }

   }
}