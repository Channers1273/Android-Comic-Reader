package com.example.comicbookreader.feature.library.domain.usecase

import com.example.comicbookreader.core.domain.model.ComicCollection
import com.example.comicbookreader.core.domain.repository.ComicRepository
import com.example.comicbookreader.feature.library.domain.model.LibraryContents
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetLibraryContentsUseCase @Inject constructor(
    private val comicRepository: ComicRepository
) {
    operator fun invoke(): Flow<LibraryContents> {
        return comicRepository.getAllComics().map { comics ->
            val (rootComics, nestedComics) = comics.partition { it.folderPath.isEmpty() }

            val folders = nestedComics
                .groupBy { it.folderPath }
                .map { (folderPath, comicsInFolder) ->
                    ComicCollection(
                        folderPath = folderPath,
                        name = folderPath.substringAfterLast('/'),
                        comicCount = comicsInFolder.size,
                        coverThumbnailPath = comicsInFolder.firstOrNull()?.thumbnailPath
                    )
                }
                .sortedBy { it.name }

            LibraryContents(
                folders = folders,
                rootComics = rootComics.sortedBy { it.title }
            )
        }
    }
}