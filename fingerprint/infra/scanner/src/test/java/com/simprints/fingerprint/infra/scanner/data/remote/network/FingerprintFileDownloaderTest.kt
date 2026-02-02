package com.simprints.fingerprint.infra.scanner.data.remote.network

import com.google.common.truth.Truth.*
import com.simprints.fingerprint.infra.scanner.data.FirmwareTestData
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.backendapi.ApiResult
import com.simprints.infra.backendapi.BackendApiClient
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class FingerprintFileDownloaderTest {
    private lateinit var fingerprintFileDownloader: FingerprintFileDownloader

    @MockK
    lateinit var backendApiClient: BackendApiClient

    @MockK
    lateinit var authStore: AuthStore

    @MockK
    lateinit var api: FileUrlRemoteInterface

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        fingerprintFileDownloader =
            FingerprintFileDownloader(
                backendApiClient,
                authStore,
                testCoroutineRule.testCoroutineDispatcher,
            )
    }

    @Test
    fun getFileUrl() = runTest {
        // Given
        coEvery { api.getFileUrl(any(), any()) } returns FileUrl(FirmwareTestData.SOME_URL)
        coEvery { backendApiClient.executeCall<FileUrlRemoteInterface, Any>(any(), any()) } coAnswers {
            try {
                ApiResult.Success(secondArg<suspend (FileUrlRemoteInterface) -> Any>()(api))
            } catch (e: Exception) {
                ApiResult.Failure(e)
            }
        }

        every { authStore.signedInProjectId } returns "projectId"
        // When
        val result = fingerprintFileDownloader.getFileUrl("Any fileId")
        // Then
        assertThat(result).isEqualTo(FirmwareTestData.SOME_URL)
    }
}
