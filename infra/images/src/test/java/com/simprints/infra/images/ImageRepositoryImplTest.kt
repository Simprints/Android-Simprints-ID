package com.simprints.infra.images

import com.google.common.truth.Truth.*
import com.simprints.core.domain.modality.Modality
import com.simprints.infra.images.local.ImageLocalDataSource
import com.simprints.infra.images.metadata.ImageMetadataStore
import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.images.remote.SampleUploader
import com.simprints.infra.images.usecase.GetUploaderUseCase
import com.simprints.infra.images.usecase.SamplePathConverter
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class ImageRepositoryImplTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var localDataSource: ImageLocalDataSource

    @MockK
    lateinit var sampleUploader: SampleUploader

    @MockK
    lateinit var metadataStore: ImageMetadataStore

    @MockK
    lateinit var samplePathConverter: SamplePathConverter

    @MockK
    lateinit var getUploaderUseCase: GetUploaderUseCase

    private lateinit var repository: ImageRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { samplePathConverter.create(any(), any(), any(), any()) } returns Path(VALID_PATH)
        coEvery { sampleUploader.uploadAllSamples(any(), any()) } returns true
        coEvery { getUploaderUseCase.invoke() } returns sampleUploader

        repository = ImageRepositoryImpl(
            localDataSource = localDataSource,
            metadataStore = metadataStore,
            samplePathConverter = samplePathConverter,
            getSampleUploader = getUploaderUseCase,
        )
    }

    @Test
    fun `save sample to local storage`() = runTest {
        coEvery { localDataSource.encryptAndStoreImage(any(), any(), any()) } returns mockValidImage()
        val imageRef = repository.storeSample(
            projectId = PROJECT_ID,
            sessionId = "sessionId",
            modality = Modality.FACE,
            sampleId = "sampleId",
            fileExtension = "jpg",
            sampleBytes = ByteArray(10),
        )

        assertThat(imageRef).isNotNull()
        coVerify {
            metadataStore.storeMetadata(any(), any())
            localDataSource.encryptAndStoreImage(any(), any(), any())
        }
    }

    @Test
    fun `save sample with metadata to local storage`() = runTest {
        coEvery { localDataSource.encryptAndStoreImage(any(), any(), any()) } returns mockValidImage()
        val imageRef = repository.storeSample(
            projectId = PROJECT_ID,
            sessionId = "sessionId",
            modality = Modality.FACE,
            sampleId = "sampleId",
            fileExtension = "jpg",
            sampleBytes = ByteArray(10),
            optionalMetadata = mapOf("k" to "v"),
        )

        assertThat(imageRef).isNotNull()
        coVerify {
            metadataStore.storeMetadata(any(), any())
            localDataSource.encryptAndStoreImage(any(), any(), any())
        }
    }

    @Test
    fun `returns null if was not able to save image`() = runTest {
        coEvery { localDataSource.encryptAndStoreImage(any(), any(), any()) } returns null
        val imageRef = repository.storeSample(
            projectId = PROJECT_ID,
            sessionId = "sessionId",
            modality = Modality.FACE,
            sampleId = "sampleId",
            fileExtension = "jpg",
            sampleBytes = ByteArray(10),
            optionalMetadata = mapOf("k" to "v"),
        )

        assertThat(imageRef).isNull()
        coVerify(exactly = 0) {
            metadataStore.storeMetadata(any(), any())
        }
    }

    @Test
    fun `delegates sample upload to uploader`() = runTest {
        val successful = repository.uploadStoredImagesAndDelete(PROJECT_ID)

        assertThat(successful).isTrue()
        coVerify { sampleUploader.uploadAllSamples(any(), any()) }
    }

    @Test
    fun `delegates sample upload to uploader with progress callback`() = runTest {
        val progressCallback: suspend (Int, Int) -> Unit = mockk(relaxed = true)
        val successful = repository.uploadStoredImagesAndDelete(PROJECT_ID, progressCallback)

        assertThat(successful).isTrue()
        coVerify { sampleUploader.uploadAllSamples(PROJECT_ID, progressCallback) }
    }

    @Test
    fun `progress callback receives correct values`() = runTest {
        var (receivedCurrent, receivedTotal) = -1 to -1
        val progressCallback: suspend (Int, Int) -> Unit = { current, total ->
            receivedCurrent = current
            receivedTotal = total
        }
        coEvery { sampleUploader.uploadAllSamples(any(), any()) } coAnswers {
            val callback = secondArg<suspend (Int, Int) -> Unit>()
            callback(3, 10)
            true
        }

        repository.uploadStoredImagesAndDelete(PROJECT_ID, progressCallback)

        assertThat(receivedCurrent).isEqualTo(3)
        assertThat(receivedTotal).isEqualTo(10)
    }

    @Test
    fun `deletes stored images and metadata`() = runTest {
        configureLocalImageFiles(numberOfValidFiles = 5)

        repository.deleteStoredImages()

        coVerify(exactly = 5) { localDataSource.deleteImage(any()) }
        coVerify(exactly = 1) { metadataStore.deleteAllMetadata() }
    }

    @Test
    fun `returns number of images to upload`() = runTest {
        configureLocalImageFiles(numberOfValidFiles = 5)

        val imageCount = repository.getNumberOfImagesToUpload(PROJECT_ID)

        assertThat(imageCount).isEqualTo(5)
    }

    private fun configureLocalImageFiles(numberOfValidFiles: Int) {
        val files = mutableListOf<SecuredImageRef>().apply {
            repeat(numberOfValidFiles) {
                add(mockValidImage())
            }
        }

        coEvery { localDataSource.listImages(PROJECT_ID) } returns files
        coEvery { localDataSource.listImages(null) } returns files
    }

    private fun mockValidImage() = SecuredImageRef(Path(VALID_PATH))

    companion object {
        private const val VALID_PATH = "valid.txt"
        private const val PROJECT_ID = "projectId"
    }
}
