package com.simprints.infra.images.remote.signedurl.usecase

import com.google.common.truth.Truth.*
import com.simprints.infra.backendapi.ApiResult
import com.simprints.infra.backendapi.BackendApiClient
import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.images.remote.signedurl.SampleUploadData
import com.simprints.infra.images.remote.signedurl.api.ApiSampleUploadUrlRequest
import com.simprints.infra.images.remote.signedurl.api.ApiSampleUploadUrlResponse
import com.simprints.infra.images.remote.signedurl.api.SampleUploadApiInterface
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class FetchUploadUrlsPerSampleUseCaseTest {
    @MockK
    lateinit var backendApiClient: BackendApiClient

    @MockK
    lateinit var apiInterface: SampleUploadApiInterface

    private lateinit var useCase: FetchUploadUrlsPerSampleUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { backendApiClient.executeCall<SampleUploadApiInterface, Any>(any(), any()) } coAnswers {
            try {
                ApiResult.Success(secondArg<suspend (SampleUploadApiInterface) -> Any>()(apiInterface))
            } catch (e: Exception) {
                ApiResult.Failure(e)
            }
        }
        useCase = FetchUploadUrlsPerSampleUseCase(backendApiClient)
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
