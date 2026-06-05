package com.example.comicbookreader.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ComicRepository(
    //private val comicDao: ComicDao = ComicDao
) {

    fun unzipComic(
        context: Context,
        comicUri: Uri
    ): Flow<Uri> {
        val inputStream = context.contentResolver.openInputStream(comicUri)
        //val pageList = mutableListOf<Uri>()

        val flow = flow<Uri> {
            ZipInputStream(inputStream).use { zip ->
                var page: ZipEntry? = zip.nextEntry

                while (page != null) {

                    if (!page.isDirectory) {
                        val pageName = page.name
                        Log.d("unzipComicRepository", "unzipped and got file $pageName || Type is ${page::class.qualifiedName}")

                        val outputFile = File(context.cacheDir, pageName)
                        outputFile.writeBytes(zip.readBytes())

                        val uri = Uri.fromFile(outputFile)
                        emit(uri)

                        zip.closeEntry()
                        page = zip.nextEntry
                    }

                }
            }
        }

        return flow
    }

}