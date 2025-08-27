package com.simprints.infra.images.remote.signedurl.usecase

import com.google.common.truth.Truth.*
import com.simprints.core.domain.image.Path
import com.simprints.core.domain.image.SecuredImageRef
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.images.remote.signedurl.SampleUploadData
import com.simprints.infra.images.remote.signedurl.api.ApiSampleUploadUrlRequest
import com.simprints.infra.images.remote.signedurl.api.ApiSampleUploadUrlResponse
import com.simprints.infra.images.remote.signedurl.api.SampleUploadApiInterface
import com.simprints.infra.network.SimNetwork
import com.simprints.testtools.common.alias.InterfaceInvocation
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.reflect.KClass

internal class FetchUploadUrlsPerSampleUseCaseTest {
    @MockK
    lateinit var authStore: AuthStore

    @MockK
    lateinit var apiClient: SimNetwork.SimApiClient<SampleUploadApiInterface>

    @MockK
    lateinit var apiInterface: SampleUploadApiInterface

    private lateinit var useCase: FetchUploadUrlsPerSampleUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { authStore.buildClient(any<KClass<SampleUploadApiInterface>>()) } returns apiClient
        coEvery { apiClient.executeCall<ApiSampleUploadUrlResponse>(any()) } coAnswers {
            val args = this.args
            @Suppress("UNCHECKED_CAST")
            (args[0] as InterfaceInvocation<SampleUploadApiInterface, ApiSampleUploadUrlResponse>).invoke(apiInterface)
        }

        useCase = FetchUploadUrlsPerSampleUseCase(authStore)
    }

    @Test
    fun `Successfully fetches upload urls for provided sample data`() = runTest {
        coEvery { apiInterface.getSampleUploadUrl(any<String>(), any<List<ApiSampleUploadUrlRequest>>()) } returns listOf(
            ApiSampleUploadUrlResponse(sampleId = "sampleId", url = "url"),
        )

        val result = useCase(PROJECT_ID, listOf(mockSampleUploadData()))

        assertThat(result["sampleId"]).isEqualTo("url")
        coVerify(exactly = 1) { apiInterface.getSampleUploadUrl(PROJECT_ID, any()) }
    }

    @Test
    fun `Gracefully handles upload url request error`() = runTest {
        coEvery { apiInterface.getSampleUploadUrl(any(), any()) } throws Exception("Failed")

        val result = useCase(PROJECT_ID, listOf(mockSampleUploadData()))

        assertThat(result).isEmpty()
    }

    private fun mockSampleUploadData() = SampleUploadData(
        imageRef = SecuredImageRef(Path("sampleId")),
        sampleId = "sampleId",
        sessionId = "sessionId",
        modality = "modality",
        md5 = "md5",
        size = 10L,
        metadata = emptyMap(),
    )

    companion object {
        private const val PROJECT_ID = "projectId"
    }
}

//  coEvery { authStore.buildClient(any<KClass<SampleUploadApiInterface>>()) } returns apiClient
//        coEvery { apiClient.executeCall<ApiSampleUploadUrlResponse>(any()) } coAnswers {
//            val args = this.args
//            @Suppress("UNCHECKED_CAST")
//            (args[0] as InterfaceInvocation<SampleUploadApiInterface, ApiSampleUploadUrlResponse>).invoke(apiInterface)
//        }
//        coEvery { apiClient.executeCall<Response<ResponseBody>>(any()) } coAnswers {
//            val args = this.args
//            @Suppress("UNCHECKED_CAST")
//            (args[0] as InterfaceInvocation<SampleUploadApiInterface, Response<ResponseBody>>).invoke(apiInterface)
//        }
