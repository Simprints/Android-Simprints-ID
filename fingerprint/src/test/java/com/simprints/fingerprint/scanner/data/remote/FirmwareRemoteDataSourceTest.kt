package com.simprints.fingerprint.scanner.data.remote

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.controllers.core.network.FingerprintApiClient
import com.simprints.fingerprint.controllers.core.network.FingerprintApiClientFactory
import com.simprints.fingerprint.controllers.core.network.FingerprintFileDownloader
import com.simprints.fingerprint.scanner.domain.versions.ChipFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
class FirmwareRemoteDataSourceTest {

    private val fingerprintApiClientMock: FingerprintApiClient<FirmwareRemoteInterface> = mockk()
    private val fingerprintApiClientFactoryMock: FingerprintApiClientFactory = mockk {
        coEvery { buildClient(eq(FirmwareRemoteInterface::class)) } returns fingerprintApiClientMock
    }
    private val fingerprintFileDownloaderMock: FingerprintFileDownloader = mockk()

    private val firmwareRemoteDataSource = FirmwareRemoteDataSource(fingerprintApiClientFactoryMock, fingerprintFileDownloaderMock)

    @Test
    fun getDownloadableFirmwares_correctlyCallsApiAndTransformsResponse() = runBlockingTest {
        coEvery { fingerprintApiClientMock.executeCall<Map<String, ApiDownloadableFirmwareVersion>>(any(), any()) } returns RESPONSE_MAP

        val response = firmwareRemoteDataSource.getDownloadableFirmwares(SCANNER_VERSIONS_LOW)

        assertThat(response).containsExactlyElementsIn(RESPONSE_MAP.values.map { it.toDomain() })
    }

    @Test
    fun getDownloadableFirmwares_withUnknownSavedVersion_correctlyCallsApiAndTransformsResponse() = runBlockingTest {
        coEvery { fingerprintApiClientMock.executeCall<Map<String, ApiDownloadableFirmwareVersion>>(any(), any()) } returns RESPONSE_MAP

        val response = firmwareRemoteDataSource.getDownloadableFirmwares(ScannerFirmwareVersions.UNKNOWN)

        assertThat(response).containsExactlyElementsIn(RESPONSE_MAP.values.map { it.toDomain() })
    }

    @Test
    fun downloadFile_correctlyForwardsDownload() = runBlockingTest {
        coEvery { fingerprintFileDownloaderMock.download(eq(SOME_URL)) } returns SOME_BIN

        val bytes = firmwareRemoteDataSource.downloadFile(SOME_URL)

        assertThat(bytes.toList()).containsExactlyElementsIn(SOME_BIN.toList()).inOrder()
    }

    companion object {
        private const val CYPRESS_NAME = "cypress"
        private val CYPRESS_VERSION_LOW = ChipFirmwareVersion(1, 0)
        private val CYPRESS_VERSION_HIGH = ChipFirmwareVersion(1, 1)
        private const val CYPRESS_URL = "cypress_url.com"

        private const val STM_NAME = "stm"
        private val STM_VERSION_LOW = ChipFirmwareVersion(1, 1)
        private val STM_VERSION_HIGH = ChipFirmwareVersion(1, 2)
        private const val STM_URL = "stm_url.com"

        private const val UN20_NAME = "un20"
        private val UN20_VERSION_LOW = ChipFirmwareVersion(1, 2)
        private val UN20_VERSION_HIGH = ChipFirmwareVersion(1, 3)
        private const val UN20_URL = "un20_url.com"

        private val CYPRESS_API_RESPONSE = ApiDownloadableFirmwareVersion(CYPRESS_NAME, CYPRESS_VERSION_HIGH.toString(), CYPRESS_URL)
        private val STM_API_RESPONSE = ApiDownloadableFirmwareVersion(STM_NAME, STM_VERSION_HIGH.toString(), STM_URL)
        private val UN20_API_RESPONSE = ApiDownloadableFirmwareVersion(UN20_NAME, UN20_VERSION_HIGH.toString(), UN20_URL)

        private val RESPONSE_MAP = mapOf(
            CYPRESS_NAME to CYPRESS_API_RESPONSE,
            STM_NAME to STM_API_RESPONSE,
            UN20_NAME to UN20_API_RESPONSE
        )

        private val SCANNER_VERSIONS_LOW = ScannerFirmwareVersions(CYPRESS_VERSION_LOW, STM_VERSION_LOW, UN20_VERSION_LOW)

        private const val SOME_URL = "some.url.com"
        private val SOME_BIN = byteArrayOf(0x00, 0x01, 0x02)
    }
}
