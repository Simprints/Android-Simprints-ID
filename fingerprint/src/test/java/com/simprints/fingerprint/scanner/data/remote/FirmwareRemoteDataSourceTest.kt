package com.simprints.fingerprint.scanner.data.remote

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.controllers.core.network.FingerprintFileDownloader
import com.simprints.fingerprint.scanner.data.FirmwareTestData
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.Vero2Configuration
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FirmwareRemoteDataSourceTest {
    private val fingerprintFileDownloaderMock: FingerprintFileDownloader = mockk()
    private val fingerprintPreferencesMock = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { fingerprint } returns mockk {
                every { vero2 } returns mockk {
                    every { firmwareVersions } returns RESPONSE_MAP
                }
            }
        }
    }

    private val firmwareRemoteDataSource =
        FirmwareRemoteDataSource(fingerprintFileDownloaderMock, fingerprintPreferencesMock)

    @Before
    fun setup() {
        coEvery { fingerprintFileDownloaderMock.getFileUrl(any()) } returns SOME_URL
    }

    @Test
    fun getDownloadableFirmwares_correctlyCallsApiAndTransformsResponse() = runTest {

        val response = firmwareRemoteDataSource.getDownloadableFirmwares(
            HARDWARE_VERSION,
            emptyMap()
        )

        assertThat(response.size).isEqualTo(3)
    }

    @Test
    fun downloadFile_correctlyForwardsDownload() = runTest {
        coEvery { fingerprintFileDownloaderMock.download(eq(SOME_URL)) } returns SOME_BIN

        val bytes = firmwareRemoteDataSource.downloadFirmware(
            DownloadableFirmwareVersion(
                DownloadableFirmwareVersion.Chip.STM,
                STM_VERSION_HIGH
            )
        )

        assertThat(bytes.toList()).containsExactlyElementsIn(SOME_BIN.toList()).inOrder()
    }

    companion object {
        private const val HARDWARE_VERSION = "E-1"
        private const val CYPRESS_VERSION_HIGH = "1.E-1.1"

        private const val STM_VERSION_HIGH = "1.E-1.2"
        private const val UN20_VERSION_HIGH = "1.E-1.3"

        private val RESPONSE_MAP = mapOf(
            FirmwareTestData.HARDWARE_VERSION to Vero2Configuration.Vero2FirmwareVersions(
                CYPRESS_VERSION_HIGH,
                STM_VERSION_HIGH,
                UN20_VERSION_HIGH
            )
        )

        private const val SOME_URL = "some.url.com"
        private val SOME_BIN = byteArrayOf(0x00, 0x01, 0x02)
    }
}
