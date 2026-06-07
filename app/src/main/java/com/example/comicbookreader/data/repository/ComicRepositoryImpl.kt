package com.example.comicbookreader.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.example.comicbookreader.data.local.ComicDao
import com.example.comicbookreader.data.model.Comic
import com.example.comicbookreader.domain.repository.ComicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.time.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream



class ComicRepositoryImpl(
    private val comicDao: ComicDao,
    private val context: Context
): ComicRepository {

    override fun unzipComic(
        comicUri: Uri
    ): Flow<Uri> {
        val inputStream = context.contentResolver.openInputStream(comicUri)

        val flow = flow<Uri> {
            ZipInputStream(inputStream).use { zip ->
                var page: ZipEntry? = zip.nextEntry

                while (page != null) {

                    if (!page.isDirectory) {
                        val pageName = page.name
                        Log.d(
                            "unzipComicRepository",
                            "unzipped and got file $pageName || Type is ${page::class.qualifiedName}"
                        )

                        val outputFile = File(context.cacheDir, pageName)
                        outputFile.writeBytes(zip.readBytes())

                        val uri = Uri.fromFile(outputFile)
                        emit(uri)

                        zip.closeEntry()
                        page = zip.nextEntry
                    }
                }
            }
        }.flowOn(Dispatchers.Default)

        return flow
    }

    private fun scanFolder(folderUri: Uri): List<Uri> {
        val comicDirectory = DocumentFile.fromTreeUri(context, folderUri) ?: return emptyList()

        return comicDirectory.listFiles()
            .filter { it.isFile && it.name?.endsWith(".cbz", ignoreCase = true) == true }
            .mapNotNull { it.uri }
    }

    private fun createComicFromUri(fileUri: Uri): Comic? {
        val documentFile = DocumentFile.fromSingleUri(context, fileUri) ?: return null

        val fileName = documentFile.name ?: return null
        val title = fileName.removeSuffix(".cbz")
        val pageCount = getNumPages(fileUri)

        return Comic (
            title = title,
            pageCount = pageCount,
            lastOpened = null,
            dateAdded = LocalDate.now().toString(),
            fileUriString = fileUri.toString(),
        )
    }

    private fun getNumPages(fileUri: Uri): Int {
        val inputStream = context.contentResolver.openInputStream(fileUri)

        var pageCount = 0
        ZipInputStream(inputStream).use { zip ->
            var page = zip.nextEntry

            while (page != null) {
                if (!page.isDirectory) {
                    pageCount++

                    zip.closeEntry()
                    page = zip.nextEntry
                }
            }

        }
        return pageCount
    }

    override suspend fun syncLibrary(folderUri: Uri) {
        val foundUris = scanFolder( folderUri)
        val existingUris = comicDao.getAllComics().map { it.fileUriString.toUri() }

        val newUris = foundUris.filter { it !in existingUris }
        newUris.forEach { uri ->
            val newComic = createComicFromUri( uri)
            if (newComic != null) comicDao.upsertComic(newComic)
        }
    }

}