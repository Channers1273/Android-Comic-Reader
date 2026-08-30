package com.example.comicbookreader.feature.library.domain.usecase

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.example.comicbookreader.fakes.FakeComicRepository
import com.example.comicbookreader.fakes.testComic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetLibraryContentsUseCaseTest {

    private lateinit var comicRepository: FakeComicRepository
    private lateinit var useCase: GetLibraryContentsUseCase

    @BeforeEach
    fun setUp() {
        comicRepository = FakeComicRepository()
        useCase = GetLibraryContentsUseCase(comicRepository)
    }

    @Test
    fun `groups comics by folder path`() = runTest {
        comicRepository.seedComic(testComic(1, folderPath = "SeriesA"))
        comicRepository.seedComic(testComic(2, folderPath = "SeriesA"))
        comicRepository.seedComic(testComic(3, folderPath = "SeriesB"))

        val contents = useCase().first()

        assertThat(contents.folders).hasSize(2)
        assertThat(contents.folders.first { it.folderPath == "SeriesA" }.comicCount).isEqualTo(2)
        assertThat(contents.folders.first { it.folderPath == "SeriesB" }.comicCount).isEqualTo(1)
    }

    @Test
    fun `comics at the root folder are returned as root comics, not a folder`() = runTest {
        comicRepository.seedComic(testComic(1, folderPath = ""))

        val contents = useCase().first()

        assertThat(contents.folders).isEmpty()
        assertThat(contents.rootComics).hasSize(1)
        assertThat(contents.rootComics.single().id).isEqualTo(1)
    }

    @Test
    fun `root comics and subfolders are both populated when both exist`() = runTest {
        comicRepository.seedComic(testComic(1, folderPath = ""))
        comicRepository.seedComic(testComic(2, folderPath = "SeriesA"))

        val contents = useCase().first()

        assertThat(contents.folders).hasSize(1)
        assertThat(contents.rootComics).hasSize(1)
    }
}