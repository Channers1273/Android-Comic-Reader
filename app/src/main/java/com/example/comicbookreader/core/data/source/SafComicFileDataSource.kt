package com.example.comicbookreader.core.data.source

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.example.comicbookreader.core.domain.error.DataError
import com.example.comicbookreader.core.domain.error.Result
import com.github.junrar.Archive
import com.github.junrar.exception.RarException
import com.github.junrar.rarfile.FileHeader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject

private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")
private val SUPPORTED_EXTENSIONS = setOf("cbz", "cbr")
private const val COMPLETE_MARKER_NAME = ".complete"
private const val TAG = "ComicExtraction"

private val naturalOrderComparator = Comparator<String> { a, b -> naturalCompare(a, b) }

private fun naturalCompare(a: String, b: String): Int {
    var i = 0
    var j = 0
    while (i < a.length && j < b.length) {
        val ca = a[i]
        val cb = b[j]
        if (ca.isDigit() && cb.isDigit()) {
            var ni = i
            while (ni < a.length && a[ni].isDigit()) ni++
            var nj = j
            while (nj < b.length && b[nj].isDigit()) nj++
            val numA = a.substring(i, ni).trimStart('0')
            val numB = b.substring(j, nj).trimStart('0')
            val cmp = if (numA.length != numB.length) numA.length - numB.length else numA.compareTo(numB)
            if (cmp != 0) return cmp
            i = ni
            j = nj
        } else {
            val cmp = ca.lowercaseChar().compareTo(cb.lowercaseChar())
            if (cmp != 0) return cmp
            i++
            j++
        }
    }
    return (a.length - i) - (b.length - j)
}

private interface ComicArchiveEntry {
    val name: String
    val isDirectory: Boolean
}

private interface ComicArchiveReader : Closeable {
    fun nextEntry(): ComicArchiveEntry?
    fun readEntryBytes(entry: ComicArchiveEntry): ByteArray
}

private class ZipComicArchiveReader(inputStream: InputStream) : ComicArchiveReader {
    private val zip = ZipInputStream(inputStream)

    override fun nextEntry(): ComicArchiveEntry? =
        zip.nextEntry?.let { Entry(it.name, it.isDirectory) }

    override fun readEntryBytes(entry: ComicArchiveEntry): ByteArray =
        zip.readBytes().also { zip.closeEntry() }

    override fun close() = zip.close()

    private data class Entry(override val name: String, override val isDirectory: Boolean) : ComicArchiveEntry
}

private class RarComicArchiveReader(inputStream: InputStream) : ComicArchiveReader {
    private val archive = Archive(inputStream)
    private var currentHeader: FileHeader? = null

    override fun nextEntry(): ComicArchiveEntry? =
        archive.nextFileHeader()?.also { currentHeader = it }?.let { Entry(it.fileName, it.isDirectory) }

    override fun readEntryBytes(entry: ComicArchiveEntry): ByteArray {
        val outputStream = ByteArrayOutputStream()
        archive.extractFile(currentHeader, outputStream)
        return outputStream.toByteArray()
    }

    override fun close() = archive.close()

    private data class Entry(override val name: String, override val isDirectory: Boolean) : ComicArchiveEntry
}

private fun openArchiveReader(fileName: String, inputStream: InputStream): ComicArchiveReader =
    if (fileName.endsWith(".cbr", ignoreCase = true)) {
        RarComicArchiveReader(inputStream)
    } else {
        ZipComicArchiveReader(inputStream)
    }

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

                child.isFile && isSupportedComicFile(child.name.orEmpty()) -> {
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
        val title = fileName.substringBeforeLast('.')

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.Error(DataError.Local.NOT_FOUND)

            var pageCount = 0
            var bestEntryName: String? = null
            var bestBytes: ByteArray? = null

            openArchiveReader(fileName, inputStream).use { reader ->
                var entry = reader.nextEntry()
                while (entry != null) {
                    if (!entry.isDirectory && isImageEntry(entry.name)) {
                        pageCount++
                        val currentBest = bestEntryName
                        if (currentBest == null || naturalOrderComparator.compare(entry.name, currentBest) < 0) {
                            bestEntryName = entry.name
                            bestBytes = reader.readEntryBytes(entry)
                        }
                    }
                    entry = reader.nextEntry()
                }
            }

            val thumbnailPath = bestBytes?.let { writeThumbnail(it, bestEntryName!!, title) }
            Result.Success(ComicFileMetadata(title, pageCount, thumbnailPath))
        } catch (e: IOException) {
            Result.Error(DataError.Local.CORRUPTED_ARCHIVE)
        } catch (e: RarException) {
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
        val fileName = DocumentFile.fromSingleUri(context, uri)?.name.orEmpty()
        val inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream == null) {
            emit(Result.Error(DataError.Local.NOT_FOUND))
            return@flow
        }

        try {
            pagesDir.mkdirs()
            val extracted = mutableListOf<Pair<String, File>>()
            var scratchIndex = 0
            openArchiveReader(fileName, inputStream).use { reader ->
                Log.d(TAG, "Extracting '$fileName' with ${reader.javaClass.simpleName}")
                var entry = reader.nextEntry()
                while (entry != null) {
                    val isImage = !entry.isDirectory && isImageEntry(entry.name)
                    Log.d(TAG, "Encountered entry '${entry.name}' (isImage=$isImage)")
                    if (isImage) {
                        val scratchFile = File(pagesDir, "extract_${scratchIndex++}.tmp")
                        scratchFile.writeBytes(reader.readEntryBytes(entry))
                        extracted += entry.name to scratchFile
                    }
                    entry = reader.nextEntry()
                }
            }

            val sorted = extracted.sortedWith(compareBy(naturalOrderComparator) { it.first })
            Log.d(TAG, "Sorted page order for '$fileName': ${sorted.map { it.first }}")

            sorted.forEachIndexed { index, (entryName, scratchFile) ->
                val entryBaseName = entryName.substringAfterLast('/').substringAfterLast('\\')
                val outputFile = File(pagesDir, "%03d_%s".format(index, entryBaseName))
                if (!scratchFile.renameTo(outputFile)) {
                    Log.w(TAG, "renameTo failed for page $index ('$entryName'); falling back to copy")
                    scratchFile.copyTo(outputFile, overwrite = true)
                    scratchFile.delete()
                }
                emit(Result.Success(outputFile.absolutePath))
            }
            markerFile.createNewFile()
        } catch (e: IOException) {
            emit(Result.Error(DataError.Local.CORRUPTED_ARCHIVE))
        } catch (e: RarException) {
            emit(Result.Error(DataError.Local.CORRUPTED_ARCHIVE))
        }
    }.flowOn(Dispatchers.IO)

    private fun writeThumbnail(bytes: ByteArray, entryName: String, title: String): String {
        val sanitizedTitle = title.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val extension = entryName.substringAfterLast('.', missingDelimiterValue = "jpg")
        val outputFile = File(context.filesDir, "thumbnail_$sanitizedTitle.$extension")
        outputFile.writeBytes(bytes)
        return outputFile.absolutePath
    }

    private fun isImageEntry(name: String): Boolean =
        name.substringAfterLast('.', missingDelimiterValue = "").lowercase() in IMAGE_EXTENSIONS
}

private fun isSupportedComicFile(name: String): Boolean =
    name.substringAfterLast('.', missingDelimiterValue = "").lowercase() in SUPPORTED_EXTENSIONS