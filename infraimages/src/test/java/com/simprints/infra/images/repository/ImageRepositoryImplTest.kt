package com.simprints.infra.images.repository
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.images.ImageRepositoryImpl
import com.simprints.infra.images.local.ImageLocalDataSource
import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.images.remote.ImageRemoteDataSource
import com.simprints.infra.images.remote.UploadResult
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.FileInputStream

internal class ImageRepositoryImplTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

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
    fun withEmptyList_shouldConsiderUploadOperationSuccessful() = runTest {
        coEvery { localDataSource.listImages(PROJECT_ID) } returns emptyList()

        val successful = repository.uploadStoredImagesAndDelete(PROJECT_ID)

        assertThat(successful).isTrue()
    }

    @Test
    fun withAllFilesValid_shouldUploadAndDeleteSuccessfully() = runTest {
        configureLocalImageFiles(includeInvalidFile = false)

        val successful = repository.uploadStoredImagesAndDelete(PROJECT_ID)

        assertThat(successful).isTrue()
    }

    @Test
    fun shouldDeleteAnImageAfterTheUpload() = runTest {
        configureLocalImageFiles(includeInvalidFile = false)

        val successful = repository.uploadStoredImagesAndDelete(PROJECT_ID)

        coVerify(exactly = 3) { localDataSource.decryptImage(any()) }
        coVerify(exactly = 3) { localDataSource.deleteImage(any()) }
        assertThat(successful).isTrue()
    }

    @Test
    fun shouldDecryptImageBeforeUploading() = runTest {
        configureLocalImageFiles(numberOfValidFiles = 5, includeInvalidFile = false)

        repository.uploadStoredImagesAndDelete(PROJECT_ID)

        coVerify(exactly = 5) { localDataSource.decryptImage(any()) }
    }

    @Test
    fun shouldDeleteStoredImages() = runTest {
        configureLocalImageFiles(numberOfValidFiles = 5, includeInvalidFile = false)

        repository.deleteStoredImages()

        coVerify(exactly = 5) { localDataSource.deleteImage(any()) }
    }

    @Test
    fun shouldGetImagesCount() = runTest {
        val nImagesInLocal = 5
        configureLocalImageFiles(numberOfValidFiles = nImagesInLocal, includeInvalidFile = false)

        val imageCount = repository.getNumberOfImagesToUpload(PROJECT_ID)

        assertThat(imageCount).isEqualTo(nImagesInLocal)
    }

    private fun initialiseMocks() {
        val validImage = mockValidImage()
        val invalidImage = mockInvalidImage()
        val mockStream = mockk<FileInputStream>()

        coEvery {
            localDataSource.deleteImage(validImage)
        } returns true

        coEvery {
            localDataSource.deleteImage(invalidImage)
        } returns false

        coEvery {
            localDataSource.decryptImage(validImage)
        } returns mockStream

        coEvery {
            localDataSource.decryptImage(invalidImage)
        } returns null

        coEvery {
            remoteDataSource.uploadImage(mockStream, validImage)
        } returns UploadResult(
            validImage,
            UploadResult.Status.SUCCESSFUL
        )

        coEvery {
            remoteDataSource.uploadImage(mockStream, invalidImage)
        } returns UploadResult(
            invalidImage,
            UploadResult.Status.FAILED
        )
    }

    private fun configureLocalImageFiles(numberOfValidFiles: Int = 3, includeInvalidFile: Boolean) {
        val files = mutableListOf<SecuredImageRef>().apply {
            repeat(numberOfValidFiles) {
                add(mockValidImage())
            }

            if (includeInvalidFile)
                add(mockInvalidImage())
        }

        coEvery { localDataSource.listImages(PROJECT_ID) } returns files
        coEvery { localDataSource.listImages(null) } returns files
    }

    private fun mockValidImage() =
        SecuredImageRef(
            Path(
                VALID_PATH
            )
        )

    private fun mockInvalidImage() =
        SecuredImageRef(
            Path(
                INVALID_PATH
            )
        )

    companion object {
        private const val VALID_PATH = "valid.txt"
        private const val INVALID_PATH = "invalid"
        private const val PROJECT_ID = "projectId"
    }

}

