package com.simprints.id.data.db.image.repository

import com.google.common.truth.Truth.assertThat
import com.simprints.core.images.Path
import com.simprints.core.images.SecuredImageRef
import com.simprints.id.data.db.image.local.ImageLocalDataSource
import com.simprints.id.data.db.image.remote.ImageRemoteDataSource
import com.simprints.id.data.db.image.remote.UploadResult
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import java.io.FileInputStream

@ExperimentalCoroutinesApi
class ImageRepositoryImplTest {

    @MockK lateinit var localDataSource: ImageLocalDataSource
    @MockK lateinit var remoteDataSource: ImageRemoteDataSource

    private lateinit var repository: ImageRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = ImageRepositoryImpl(localDataSource, remoteDataSource)
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
        } returns UploadResult(validImage, UploadResult.Status.SUCCESSFUL)

        coEvery {
            remoteDataSource.uploadImage(mockStream, invalidImage)
        } returns UploadResult(invalidImage, UploadResult.Status.FAILED)
    }

    private fun configureLocalImageFiles(numberOfValidFiles: Int = 3, includeInvalidFile: Boolean) {
        val files = mutableListOf<SecuredImageRef>().apply {
            repeat(numberOfValidFiles) {
                add(mockValidImage())
            }

            if (includeInvalidFile)
                add(mockInvalidImage())
        }

        every { localDataSource.listImages() } returns files
    }

    private fun mockValidImage() = SecuredImageRef(Path(VALID_PATH))

    private fun mockInvalidImage() = SecuredImageRef(Path(INVALID_PATH))

    companion object {
        private const val VALID_PATH = "valid.txt"
        private const val INVALID_PATH = "invalid"
    }

}
