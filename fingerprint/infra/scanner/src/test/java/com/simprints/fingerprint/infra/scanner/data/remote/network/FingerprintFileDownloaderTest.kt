package com.simprints.fingerprint.infra.scanner.data.remote.network

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.data.FirmwareTestData
import com.simprints.infra.authstore.AuthStore
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class FingerprintFileDownloaderTest {
    private lateinit var fingerprintFileDownloader: FingerprintFileDownloader

    @MockK
    lateinit var fingerprintApiClientFactory: FingerprintApiClientFactory

    @MockK
    lateinit var authStore: AuthStore

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        fingerprintFileDownloader =
            FingerprintFileDownloader(
                fingerprintApiClientFactory,
                authStore,
                testCoroutineRule.testCoroutineDispatcher,
            )
    }

    @Test
    fun getFileUrl() = runTest(UnconfinedTestDispatcher()) {
        // Given
        val apiClient: FingerprintApiClient<FileUrlRemoteInterface> = mockk()
        val api: FileUrlRemoteInterface = mockk()
        coEvery { fingerprintApiClientFactory.buildClient<FileUrlRemoteInterface>(any()) } returns apiClient
        every { apiClient.api } returns api
        coEvery { api.getFileUrl(any(), any()) } returns FileUrl(FirmwareTestData.SOME_URL)
        every { authStore.signedInProjectId } returns "projectId"
        // When
        val result = fingerprintFileDownloader.getFileUrl("Any fileId")
        // Then
        assertThat(result).isEqualTo(FirmwareTestData.SOME_URL)
    }
}
