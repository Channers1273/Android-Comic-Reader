package com.example.comicbookreader.core.data.source

import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.example.comicbookreader.core.domain.error.DataError
import com.example.comicbookreader.core.domain.error.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject

private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")
private const val COMPLETE_MARKER_NAME = ".complete"

class SafComicFileDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) : ComicFileDataSource {

    override suspend fun scanForComicFiles(
        rootFolderUri: String
    ): Result<List<ScannedComicFile>, DataError.Local> = withContext(Dispatchers.IO) {
        val rootDocument = DocumentFile.fromTreeUri(context, rootFolderUri.toUri())
            ?: return@withContext Result.Error(DataError.Local.NOT_FOUND)

        try {
            val found = mutableListOf<ScannedComicFile>()
            collectComicFiles(rootDocument, relativePath = "", into = found)
            Result.Success(found)
        } catch (e: SecurityException) {
            Result.Error(DataError.Local.PERMISSION_DENIED)
        }
    }

    private fun collectComicFiles(
        folder: DocumentFile,
        relativePath: String,
        into: MutableList<ScannedComicFile>
    ) {
        folder.listFiles().forEach { child ->
            when {
                child.isDirectory -> {
                    val childPath = if (relativePath.isEmpty()) {
                        child.name.orEmpty()
                    } else {
                        "$relativePath/${child.name.orEmpty()}"
                    }
                    collectComicFiles(child, childPath, into)
                }

                child.isFile && child.name?.endsWith(".cbz", ignoreCase = true) == true -> {
                    into += ScannedComicFile(fileUri = child.uri.toString(), folderPath = relativePath)
                }
            }
        }
    }

    override suspend fun readMetadata(
        fileUri: String
    ): Result<ComicFileMetadata, DataError.Local> = withContext(Dispatchers.IO) {
        val uri = fileUri.toUri()
        val documentFile = DocumentFile.fromSingleUri(context, uri)
            ?: return@withContext Result.Error(DataError.Local.NOT_FOUND)
        val fileName = documentFile.name
            ?: return@withContext Result.Error(DataError.Local.NOT_FOUND)
        val title = fileName.removeSuffix(".cbz")

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.Error(DataError.Local.NOT_FOUND)

            var pageCount = 0
            var thumbnailPath: String? = null

            ZipInputStream(inputStream).use { zip ->
                var entry: ZipEntry? = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && isImageEntry(entry.name)) {
                        pageCount++
                        if (thumbnailPath == null) {
                            thumbnailPath = extractThumbnail(zip, title, entry.name)
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }

            Result.Success(ComicFileMetadata(title, pageCount, thumbnailPath))
        } catch (e: IOException) {
            Result.Error(DataError.Local.CORRUPTED_ARCHIVE)
        }
    }

    override fun unzipPages(
        comicId: Int,
        comicFileUri: String
    ): Flow<Result<String, DataError.Local>> = flow<Result<String, DataError.Local>> {
        val pagesDir = File(context.filesDir, "pages/$comicId")
        val markerFile = File(pagesDir, COMPLETE_MARKER_NAME)

        if (markerFile.exists()) {
            pagesDir.listFiles()
                ?.filter { it.name != COMPLETE_MARKER_NAME && isImageEntry(it.name) }
                ?.sortedBy { it.name }
                ?.forEach { emit(Result.Success(it.absolutePath)) }
            return@flow
        }

        val uri = comicFileUri.toUri()
        val inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream == null) {
            emit(Result.Error(DataError.Local.NOT_FOUND))
            return@flow
        }

        try {
            pagesDir.mkdirs()
            val existingFileNames = pagesDir.list()?.toSet() ?: emptySet()
            var index = 0
            ZipInputStream(inputStream).use { zip ->
                var entry: ZipEntry? = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && isImageEntry(entry.name)) {
                        val fileName = "%03d_%s".format(index++, entry.name.substringAfterLast('/'))
                        val outputFile = File(pagesDir, fileName)
                        if (fileName !in existingFileNames) {
                            val tempFile = File(pagesDir, "$fileName.tmp")
                            tempFile.writeBytes(zip.readBytes())
                            tempFile.renameTo(outputFile)
                        }
                        emit(Result.Success(outputFile.absolutePath))
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
            markerFile.createNewFile()
        } catch (e: IOException) {
            emit(Result.Error(DataError.Local.CORRUPTED_ARCHIVE))
        }
    }.flowOn(Dispatchers.IO)

    private fun extractThumbnail(zip: ZipInputStream, title: String, entryName: String): String {
        val sanitizedTitle = title.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val extension = entryName.substringAfterLast('.', missingDelimiterValue = "jpg")
        val outputFile = File(context.filesDir, "thumbnail_$sanitizedTitle.$extension")
        outputFile.writeBytes(zip.readBytes())
        return outputFile.absolutePath
    }

    private fun isImageEntry(name: String): Boolean =
        name.substringAfterLast('.', missingDelimiterValue = "").lowercase() in IMAGE_EXTENSIONS
}