package com.simprints.infra.images.remote.signedurl.usecase

import com.google.common.truth.Truth.*
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.samples.SampleUpSyncRequestEvent
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.images.local.ImageLocalDataSource
import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.images.remote.signedurl.SampleUploadData
import com.simprints.infra.images.remote.signedurl.api.ApiSampleUploadUrlResponse
import com.simprints.infra.images.remote.signedurl.api.SampleUploadApiInterface
import com.simprints.infra.network.SimNetwork
import com.simprints.testtools.common.alias.InterfaceInvocation
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.FileInputStream
import java.io.IOException
import kotlin.reflect.KClass

internal class UploadSampleWithTrackingUseCaseTest {
    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var authStore: AuthStore

    @MockK
    lateinit var apiClient: SimNetwork.SimApiClient<SampleUploadApiInterface>

    @MockK
    lateinit var apiInterface: SampleUploadApiInterface

    @MockK
    lateinit var localDataSource: ImageLocalDataSource

    @MockK
    lateinit var fileStream: FileInputStream

    @MockK
    lateinit var eventRepository: EventRepository

    @MockK
    lateinit var eventScope: EventScope
    private lateinit var useCase: UploadSampleWithTrackingUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.now() } returns Timestamp(1L)
        coEvery { authStore.buildClient(any<KClass<SampleUploadApiInterface>>()) } returns apiClient
        coEvery { apiClient.executeCall<ApiSampleUploadUrlResponse>(any()) } coAnswers {
            val args = this.args
            @Suppress("UNCHECKED_CAST")
            (args[0] as InterfaceInvocation<SampleUploadApiInterface, ApiSampleUploadUrlResponse>).invoke(apiInterface)
        }

        useCase = UploadSampleWithTrackingUseCase(
            timeHelper = timeHelper,
            authStore = authStore,
            localDataSource = localDataSource,
            eventRepository = eventRepository,
        )
    }

    @Test
    fun `Successfully upload sample and reports event`() = runTest {
        coEvery { localDataSource.decryptImage(any()) } returns fileStream
        coEvery { apiInterface.uploadFile(any(), any(), any(), any()) } returns mockk {
            every { isSuccessful } returns true
        }

        val result = useCase(eventScope, "url", mockUploadData())

        coVerify(exactly = 1) {
            eventRepository.addOrUpdateEvent(eventScope, match { it is SampleUpSyncRequestEvent })
        }

        assertThat(result).isTrue()
    }

    @Test
    fun `Handles image decryption failure`() = runTest {
        coEvery { localDataSource.decryptImage(any()) } returns null

        val result = useCase(eventScope, "url", mockUploadData())

        assertThat(result).isFalse()
    }

    @Test
    fun `Handles image upload failure`() = runTest {
        coEvery { localDataSource.decryptImage(any()) } returns fileStream
        coEvery { apiInterface.uploadFile(any(), any(), any(), any()) } returns mockk {
            every { isSuccessful } returns false
            every { errorBody()?.string() } returns "Failure"
        }

        val result = useCase(eventScope, "url", mockUploadData())

        assertThat(result).isFalse()
    }

    @Test
    fun `Handles image upload exception`() = runTest {
        coEvery { localDataSource.decryptImage(any()) } returns fileStream
        coEvery { apiInterface.uploadFile(any(), any(), any(), any()) } throws IOException("Failure")

        val result = useCase(eventScope, "url", mockUploadData())

        assertThat(result).isFalse()
    }

    private fun mockUploadData(): SampleUploadData = SampleUploadData(
        imageRef = SecuredImageRef(Path("sampleId")),
        sampleId = "sampleId",
        sessionId = "sessionId",
        modality = "modality",
        md5 = "md5",
        size = 10L,
        metadata = emptyMap(),
    )
}
