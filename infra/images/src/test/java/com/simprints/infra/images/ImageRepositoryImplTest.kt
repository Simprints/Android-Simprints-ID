package com.simprints.infra.images

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.images.local.ImageLocalDataSource
import com.simprints.infra.images.metadata.ImageMetadataStore
import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.images.remote.ImageRemoteDataSource
import com.simprints.infra.images.remote.UploadResult
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
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

    @MockK
    lateinit var localDataSource: ImageLocalDataSource

    @MockK
    lateinit var remoteDataSource: ImageRemoteDataSource

    @MockK
    lateinit var metadataStore: ImageMetadataStore

    private lateinit var repository: ImageRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = ImageRepositoryImpl(localDataSource, remoteDataSource, metadataStore)
        initValidImageMocks()
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
    fun withException_shouldReturnNotSuccessful() = runTest {
        coEvery {
            localDataSource.decryptImage(mockThrowingImage())
        } throws Exception("Cannot decrypt")

        coEvery { localDataSource.listImages(any()) } returns listOf(
            mockValidImage(),
            mockThrowingImage(),
        )

        val successful = repository.uploadStoredImagesAndDelete(PROJECT_ID)
        assertThat(successful).isFalse()
    }

    @Test
    fun withDecryptedAndNotUploadedImage_shouldReturnNotSuccessful() = runTest {
        initValidImageFailedUploadMocks()
        configureLocalImageFiles(includeInvalidFile = true)

        val successful = repository.uploadStoredImagesAndDelete(PROJECT_ID)

        assertThat(successful).isFalse()
    }

    @Test
    fun shouldDeleteAnImageAfterTheUpload() = runTest {
        configureLocalImageFiles(includeInvalidFile = false)

        val successful = repository.uploadStoredImagesAndDelete(PROJECT_ID)

        coVerify(exactly = 3) { localDataSource.decryptImage(any()) }
        coVerify(exactly = 3) { localDataSource.deleteImage(any()) }
        coVerify(exactly = 3) { metadataStore.deleteMetadata(any()) }
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
        coVerify(exactly = 1) { metadataStore.deleteAllMetadata() }
    }

    @Test
    fun shouldGetImagesCount() = runTest {
        val nImagesInLocal = 5
        configureLocalImageFiles(numberOfValidFiles = nImagesInLocal, includeInvalidFile = false)

        val imageCount = repository.getNumberOfImagesToUpload(PROJECT_ID)

        assertThat(imageCount).isEqualTo(nImagesInLocal)
    }

    private fun initValidImageMocks() {
        val validImage = mockValidImage()
        val mockStream = mockk<FileInputStream>()

        coEvery {
            localDataSource.deleteImage(validImage)
        } returns true

        coEvery {
            localDataSource.decryptImage(validImage)
        } returns mockStream

        coJustRun { metadataStore.storeMetadata(any(), any()) }
        coEvery { metadataStore.getMetadata(any()) } returns emptyMap()
        coJustRun { metadataStore.deleteMetadata(any()) }
        coJustRun { metadataStore.deleteAllMetadata() }

        coEvery {
            remoteDataSource.uploadImage(mockStream, validImage, emptyMap())
        } returns UploadResult(
            validImage,
            UploadResult.Status.SUCCESSFUL
        )
    }

    private fun initValidImageFailedUploadMocks() {
        val invalidImage = mockInvalidImage()
        val mockStream = mockk<FileInputStream>()

        coEvery {
            localDataSource.decryptImage(invalidImage)
        } returns mockStream

        coEvery {
            remoteDataSource.uploadImage(mockStream, invalidImage, emptyMap())
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

    private fun mockValidImage() = SecuredImageRef(Path(VALID_PATH))

    private fun mockInvalidImage() = SecuredImageRef(Path(INVALID_PATH))

    private fun mockThrowingImage() = SecuredImageRef(Path(THROWING_PATH))

    companion object {
        private const val VALID_PATH = "valid.txt"
        private const val INVALID_PATH = "invalid"
        private const val THROWING_PATH = "throw.exe"
        private const val PROJECT_ID = "projectId"
    }

}

