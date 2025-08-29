package com.simprints.infra.images.remote.signedurl

import com.google.common.truth.Truth.*
import com.simprints.core.domain.image.Path
import com.simprints.core.domain.image.SecuredImageRef
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.scope.EventScopeEndCause
import com.simprints.infra.images.local.ImageLocalDataSource
import com.simprints.infra.images.metadata.ImageMetadataStore
import com.simprints.infra.images.remote.signedurl.usecase.FetchUploadUrlsPerSampleUseCase
import com.simprints.infra.images.remote.signedurl.usecase.PrepareImageUploadDataUseCase
import com.simprints.infra.images.remote.signedurl.usecase.UploadSampleWithTrackingUseCase
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okio.IOException
import org.junit.Before
import kotlin.test.Test

internal class SignedUrlSampleUploaderTest {
    @MockK
    lateinit var configRepository: ConfigRepository

    @MockK
    lateinit var localDataSource: ImageLocalDataSource

    @MockK
    lateinit var eventRepository: EventRepository

    @MockK
    lateinit var metadataStore: ImageMetadataStore

    @MockK
    lateinit var fetchUploadUrlsPerSampleUseCase: FetchUploadUrlsPerSampleUseCase

    @MockK
    lateinit var uploadSampleWithTrackingUseCase: UploadSampleWithTrackingUseCase

    @MockK
    lateinit var prepareImageUploadDataUseCase: PrepareImageUploadDataUseCase

    private lateinit var signedUrlSampleUploader: SignedUrlSampleUploader

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { eventRepository.createEventScope(any(), any()) } returns mockk {
            every { id } returns "scopeId"
        }
        coJustRun { eventRepository.closeEventScope(any<EventScope>(), any<EventScopeEndCause>()) }

        signedUrlSampleUploader = SignedUrlSampleUploader(
            configRepository = configRepository,
            localDataSource = localDataSource,
            eventRepository = eventRepository,
            metadataStore = metadataStore,
            prepareImageUploadData = prepareImageUploadDataUseCase,
            uploadSampleWithTracking = uploadSampleWithTrackingUseCase,
            fetchUploadUrlsPerSample = fetchUploadUrlsPerSampleUseCase,
        )
    }

    @Test
    fun `Successfully uploads a single sample to signed url`() = runTest {
        mockBatchSize(1)
        coEvery { localDataSource.listImages(any()) } returns listOf(mockImageRef(SAMPLE_ID))
        coEvery { prepareImageUploadDataUseCase(any()) } returns mockSampleUploadData(SAMPLE_ID)
        coEvery { fetchUploadUrlsPerSampleUseCase(any(), any()) } returns mapOf(SAMPLE_ID to SIGNED_URL)
        coEvery { uploadSampleWithTrackingUseCase(any(), any(), any()) } returns true

        assertThat(signedUrlSampleUploader.uploadAllSamples(PROJECT_ID)).isTrue()

        coVerify(exactly = 1) {
            // Scope was created and closed
            eventRepository.createEventScope(any(), any())
            eventRepository.closeEventScope(any<EventScope>(), EventScopeEndCause.WORKFLOW_ENDED)

            // sample uploaded to provided url
            prepareImageUploadDataUseCase(any())
            uploadSampleWithTrackingUseCase.invoke(any(), SIGNED_URL, any())

            // Local data was cleared
            localDataSource.deleteImage(any())
            metadataStore.deleteMetadata(any())
        }
    }

    @Test
    fun `Successfully uploads a multiple samples in batches`() = runTest {
        mockBatchSize(2)
        coEvery { localDataSource.listImages(any()) } returns List(5) { mockImageRef(SAMPLE_ID) }
        coEvery { prepareImageUploadDataUseCase(any()) } answers {
            val sampleId = (it.invocation.args[0] as SecuredImageRef).relativePath.toString()
            mockSampleUploadData(sampleId)
        }
        coEvery { fetchUploadUrlsPerSampleUseCase(any(), any()) } returns mapOf(SAMPLE_ID to SIGNED_URL)
        coEvery { uploadSampleWithTrackingUseCase(any(), any(), any()) } returns true

        assertThat(signedUrlSampleUploader.uploadAllSamples(PROJECT_ID)).isTrue()

        // Single event scoper per call
        coVerify(exactly = 1) {
            eventRepository.createEventScope(any(), any())
            eventRepository.closeEventScope(any<EventScope>(), EventScopeEndCause.WORKFLOW_ENDED)
        }

        // Once per batch
        coVerify(exactly = 3) {
            fetchUploadUrlsPerSampleUseCase(any(), any())
        }

        // Once per sample
        coVerify(exactly = 5) {
            prepareImageUploadDataUseCase(any())
            uploadSampleWithTrackingUseCase.invoke(any(), SIGNED_URL, any())
            localDataSource.deleteImage(any())
            metadataStore.deleteMetadata(any())
        }
    }

    @Test
    fun `Gracefully skips failed upload data collection`() = runTest {
        mockBatchSize(3)
        coEvery { localDataSource.listImages(any()) } returns List(3) { mockImageRef("${SAMPLE_ID}_$it") }
        coEvery { prepareImageUploadDataUseCase(any()) } answers {
            val sampleId = (it.invocation.args[0] as SecuredImageRef).relativePath.toString()
            if (sampleId == "${SAMPLE_ID}_1") null else mockSampleUploadData(sampleId)
        }
        coEvery { fetchUploadUrlsPerSampleUseCase(any(), any()) } returns mapOf(
            "${SAMPLE_ID}_0" to SIGNED_URL,
            "${SAMPLE_ID}_2" to SIGNED_URL,
        )
        coEvery { uploadSampleWithTrackingUseCase(any(), any(), any()) } returns true

        assertThat(signedUrlSampleUploader.uploadAllSamples(PROJECT_ID)).isFalse()

        coVerify(exactly = 1) {
            fetchUploadUrlsPerSampleUseCase(any(), match { it.size == 2 })
        }
        // Once per sample
        coVerify(exactly = 2) {
            uploadSampleWithTrackingUseCase.invoke(any(), SIGNED_URL, any())
            localDataSource.deleteImage(any())
            metadataStore.deleteMetadata(any())
        }
    }

    @Test
    fun `Gracefully skips throwing upload data collection`() = runTest {
        mockBatchSize(3)
        coEvery { localDataSource.listImages(any()) } returns List(3) { mockImageRef("${SAMPLE_ID}_$it") }
        coEvery { prepareImageUploadDataUseCase(any()) } answers {
            val sampleId = (it.invocation.args[0] as SecuredImageRef).relativePath.toString()
            if (sampleId == "${SAMPLE_ID}_1") throw IOException("Failed") else mockSampleUploadData(sampleId)
        }
        coEvery { fetchUploadUrlsPerSampleUseCase(any(), any()) } returns mapOf(
            "${SAMPLE_ID}_0" to SIGNED_URL,
            "${SAMPLE_ID}_2" to SIGNED_URL,
        )
        coEvery { uploadSampleWithTrackingUseCase(any(), any(), any()) } returns true

        assertThat(signedUrlSampleUploader.uploadAllSamples(PROJECT_ID)).isFalse()

        coVerify(exactly = 1) {
            fetchUploadUrlsPerSampleUseCase(any(), match { it.size == 2 })
        }
        // Once per sample
        coVerify(exactly = 2) {
            uploadSampleWithTrackingUseCase.invoke(any(), SIGNED_URL, any())
            localDataSource.deleteImage(any())
            metadataStore.deleteMetadata(any())
        }
    }

    @Test
    fun `Gracefully skips missing url`() = runTest {
        mockBatchSize(3)
        coEvery { localDataSource.listImages(any()) } returns List(3) { mockImageRef("${SAMPLE_ID}_$it") }
        coEvery { prepareImageUploadDataUseCase(any()) } answers {
            val sampleId = (it.invocation.args[0] as SecuredImageRef).relativePath.toString()
            mockSampleUploadData(sampleId)
        }

        coEvery { fetchUploadUrlsPerSampleUseCase(any(), any()) } returns mapOf(
            "${SAMPLE_ID}_0" to SIGNED_URL,
            // No sampleId_1
            "${SAMPLE_ID}_2" to SIGNED_URL,
        )

        coEvery { uploadSampleWithTrackingUseCase(any(), any(), any()) } returns true

        val result = signedUrlSampleUploader.uploadAllSamples(PROJECT_ID)

        assertThat(result).isFalse()

        // Once per sample (skipping sample_1)
        coVerify(exactly = 2) {
            uploadSampleWithTrackingUseCase.invoke(any(), SIGNED_URL, any())
            localDataSource.deleteImage(any())
            metadataStore.deleteMetadata(any())
        }
    }

    @Test
    fun `Do not delete local data if upload fails`() = runTest {
        mockBatchSize(1)
        coEvery { localDataSource.listImages(any()) } returns listOf(mockImageRef(SAMPLE_ID))
        coEvery { prepareImageUploadDataUseCase(any()) } returns mockSampleUploadData(SAMPLE_ID)
        coEvery { fetchUploadUrlsPerSampleUseCase(any(), any()) } returns mapOf(SAMPLE_ID to SIGNED_URL)
        coEvery { uploadSampleWithTrackingUseCase(any(), any(), any()) } returns false

        assertThat(signedUrlSampleUploader.uploadAllSamples(PROJECT_ID)).isFalse()

        coVerify(exactly = 0) {
            localDataSource.deleteImage(any())
            metadataStore.deleteMetadata(any())
        }
    }

    @Test
    fun `progress callback receives correct index counter values during upload`() = runTest {
        val progressValues = mutableListOf<Pair<Int, Int>>()
        val progressCallback: suspend (Int, Int) -> Unit = { current, total ->
            progressValues.add(current to total)
        }
        mockBatchSize(1)
        coEvery { localDataSource.listImages(any()) } returns List(3) { mockImageRef("${SAMPLE_ID}_$it") }

        signedUrlSampleUploader.uploadAllSamples(PROJECT_ID, progressCallback)

        assertThat(progressValues).hasSize(3)
        assertThat(progressValues[0]).isEqualTo(0 to 3)
        assertThat(progressValues[1]).isEqualTo(1 to 3)
        assertThat(progressValues[2]).isEqualTo(2 to 3)
    }

    private fun mockBatchSize(batchSize: Int) {
        coEvery {
            configRepository
                .getProjectConfiguration()
                .synchronization.samples.signedUrlBatchSize
        } returns batchSize
    }

    private fun mockImageRef(path: String) = SecuredImageRef(
        relativePath = Path(path),
    )

    private fun mockSampleUploadData(sampleId: String) = SampleUploadData(
        imageRef = mockImageRef(sampleId),
        sampleId = sampleId,
        sessionId = "sessionId",
        modality = "modality",
        md5 = "md5",
        size = 10L,
        metadata = emptyMap(),
    )

    companion object {
        private const val SAMPLE_ID = "sampleId"
        private const val PROJECT_ID = "projectId"
        private const val SIGNED_URL = "signed.url"
    }
}
