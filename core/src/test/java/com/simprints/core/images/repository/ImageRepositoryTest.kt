package com.simprints.core.images.repository

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.images.local.ImageLocalDataSource
import com.simprints.id.data.images.model.Path
import com.simprints.id.data.images.model.SecuredImageRef
import com.simprints.id.data.images.remote.ImageRemoteDataSource
import com.simprints.id.data.images.remote.UploadResult
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import java.io.FileInputStream

@ExperimentalCoroutinesApi
internal class ImageRepositoryTest {

    @MockK lateinit var localDataSource: com.simprints.id.data.images.local.ImageLocalDataSource
    @MockK lateinit var remoteDataSource: com.simprints.id.data.images.remote.ImageRemoteDataSource

    private lateinit var repository: com.simprints.id.data.images.repository.ImageRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = com.simprints.id.data.images.repository.ImageRepositoryImpl(localDataSource, remoteDataSource)
        initialiseMocks()
    }

    @Test
    fun withEmptyList_shouldConsiderUploadOperationSuccessful() = runBlockingTest {
        every { localDataSource.listImages() } returns emptyList()

        val successful = repository.uploadStoredImagesAndDelete()

        assertThat(successful).isTrue()
    }

    @Test
    fun withAllFilesValid_shouldUploadAndDeleteSuccessfully() = runBlockingTest {
        configureLocalImageFiles(includeInvalidFile = false)

        val successful = repository.uploadStoredImagesAndDelete()

        assertThat(successful).isTrue()
    }

    @Test
    fun withNotAllFilesValid_shouldNotConsiderUploadOperationSuccessful() = runBlockingTest {
        configureLocalImageFiles(includeInvalidFile = true)

        val successful = repository.uploadStoredImagesAndDelete()

        assertThat(successful).isFalse()
    }

    @Test
    fun shouldDecryptImageBeforeUploading() = runBlockingTest {
        configureLocalImageFiles(numberOfValidFiles = 5, includeInvalidFile = false)

        repository.uploadStoredImagesAndDelete()

        verify(exactly = 5) { localDataSource.decryptImage(any()) }
    }

    private fun initialiseMocks() {
        val validImage = mockValidImage()
        val invalidImage = mockInvalidImage()
        val mockStream = mockk<FileInputStream>()

        every {
            localDataSource.deleteImage(validImage)
        } returns true

        every {
            localDataSource.deleteImage(invalidImage)
        } returns false

        every {
            localDataSource.decryptImage(validImage)
        } returns mockStream

        every {
            localDataSource.decryptImage(invalidImage)
        } returns null

        coEvery {
            remoteDataSource.uploadImage(mockStream, validImage)
        } returns com.simprints.id.data.images.remote.UploadResult(
                validImage,
                com.simprints.id.data.images.remote.UploadResult.Status.SUCCESSFUL
        )

        coEvery {
            remoteDataSource.uploadImage(mockStream, invalidImage)
        } returns com.simprints.id.data.images.remote.UploadResult(
                invalidImage,
                com.simprints.id.data.images.remote.UploadResult.Status.FAILED
        )
    }

    private fun configureLocalImageFiles(numberOfValidFiles: Int = 3, includeInvalidFile: Boolean) {
        val files = mutableListOf<com.simprints.id.data.images.model.SecuredImageRef>().apply {
            repeat(numberOfValidFiles) {
                add(mockValidImage())
            }

            if (includeInvalidFile)
                add(mockInvalidImage())
        }

        every { localDataSource.listImages() } returns files
    }

    private fun mockValidImage() =
            com.simprints.id.data.images.model.SecuredImageRef(
                    com.simprints.id.data.images.model.Path(
                            VALID_PATH
                    )
            )

    private fun mockInvalidImage() =
            com.simprints.id.data.images.model.SecuredImageRef(
                    com.simprints.id.data.images.model.Path(
                            INVALID_PATH
                    )
            )

    companion object {
        private const val VALID_PATH = "valid.txt"
        private const val INVALID_PATH = "invalid"
    }

}
