package com.example.comicbookreader.data.repository

import android.R.attr.thumbnail
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.navigation3.runtime.metadata
import com.example.comicbookreader.data.local.ComicDao
import com.example.comicbookreader.data.model.Comic
import com.example.comicbookreader.domain.repository.ComicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import java.io.File
import java.time.LocalDateTime
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
                    }
                    zip.closeEntry()
                    page = zip.nextEntry
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
        val metaData = getFileMetadata(fileUri, fileName)

        return Comic (
            title = title,
            pageCount = metaData.pageCount,
            lastOpened = null,
            dateAdded = LocalDateTime.now(),
            fileUri = fileUri,
            thumbnailPath = metaData.thumbnailPath
        )
    }

    private fun getFileMetadata(fileUri: Uri, fileName: String): ComicMetadata {
        val inputStream = context.contentResolver.openInputStream(fileUri)

        var pageCount = 0

        var thumbnailPath = ""

        ZipInputStream(inputStream).use { zip ->
            var page = zip.nextEntry
            var hasExtractedThumbnail = false

            while (page != null) {
                if (!page.isDirectory) {
                    pageCount++

                    if (!hasExtractedThumbnail) {
                        val outputFile = File(context.filesDir, "thumbnail- + ${fileName.substringBeforeLast(".")} + .png")
                        outputFile.writeBytes(zip.readBytes())
                        thumbnailPath = outputFile.absolutePath
                        hasExtractedThumbnail = true
                    }

                    zip.closeEntry()
                    page = zip.nextEntry
                }
            }

        }
        return ComicMetadata(pageCount, thumbnailPath)
    }

    override suspend fun syncLibrary(folderUri: Uri) {
        val foundUris: List<Uri> = scanFolder(folderUri)

        val existingUris = comicDao.getAllComicsOnce().map {
            it.fileUri
        }

        val newUris = foundUris.filter { it !in existingUris }
        newUris.forEach { uri ->
            val newComic = createComicFromUri( uri)
            if (newComic != null) comicDao.upsertComic(newComic)
        }
    }

    override fun getAllComics(): Flow<List<Comic>> {
        return comicDao.getAllComics()
    }

}

data class ComicMetadata(
    val pageCount: Int,
    val thumbnailPath: String
)

