package com.simprints.fingerprint.scanner.data.remote

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.controllers.core.network.FingerprintFileDownloader
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerHardwareRevisions
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class FirmwareRemoteDataSourceTest {
    private val fingerprintFileDownloaderMock: FingerprintFileDownloader = mockk()
    private val fingerprintPreferencesMock: FingerprintPreferencesManager = mockk()

    private val firmwareRemoteDataSource =
        FirmwareRemoteDataSource(fingerprintFileDownloaderMock, fingerprintPreferencesMock)

    @Before
    fun setup() {
        every { fingerprintPreferencesMock.scannerHardwareRevisions } returns RESPONSE_MAP
        coEvery { fingerprintFileDownloaderMock.getFileUrl(any()) } returns SOME_URL
    }

    @Test
    fun getDownloadableFirmwares_correctlyCallsApiAndTransformsResponse() = runBlockingTest {

        val response = firmwareRemoteDataSource.getDownloadableFirmwares(
            HARDWARE_VERSION,
            ScannerFirmwareVersions.UNKNOWN
        )

        assertThat(response.size).isEqualTo(3)
    }

    @Test
    fun downloadFile_correctlyForwardsDownload() = runBlockingTest {
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

        private val scannerFirmwareVersions =
            ScannerFirmwareVersions(
                cypress = CYPRESS_VERSION_HIGH,
                stm = STM_VERSION_HIGH,
                un20 = UN20_VERSION_HIGH,
            )
        private val RESPONSE_MAP = ScannerHardwareRevisions().apply {
            put(HARDWARE_VERSION, scannerFirmwareVersions)
        }

        private const val SOME_URL = "some.url.com"
        private val SOME_BIN = byteArrayOf(0x00, 0x01, 0x02)
    }
}
