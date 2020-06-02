package com.simprints.fingerprint.scanner.data

import com.simprints.fingerprint.controllers.core.network.FingerprintApiClient
import com.simprints.fingerprint.controllers.core.network.FingerprintApiClientFactory
import com.simprints.fingerprint.controllers.core.network.FingerprintFileDownloader
import com.simprints.fingerprint.scanner.data.local.FirmwareFileManager
import com.simprints.fingerprint.scanner.data.remote.ApiFirmwareVersionResponse
import com.simprints.fingerprint.scanner.data.remote.DownloadableFirmwareVersion
import com.simprints.fingerprint.scanner.data.remote.FirmwareRemoteInterface
import com.simprints.fingerprint.scanner.data.remote.toDomain
import com.simprints.fingerprint.scanner.domain.versions.ChipFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
class FirmwareFileUpdaterTest {

    private val fingerprintApiClientMock: FingerprintApiClient<FirmwareRemoteInterface> = mockk()
    private val fingerprintApiClientFactoryMock: FingerprintApiClientFactory = mockk {
        coEvery { buildClient(eq(FirmwareRemoteInterface::class)) } returns fingerprintApiClientMock
    }
    private val fingerprintFileDownloaderMock: FingerprintFileDownloader = mockk()
    private val firmwareFileManagerMock: FirmwareFileManager = mockk(relaxUnitFun = true)

    private val firmwareFileUpdater = FirmwareFileUpdater(fingerprintApiClientFactoryMock, fingerprintFileDownloaderMock, firmwareFileManagerMock)

    @Test
    fun download_versionsAvailable_downloadsAndSavesFiles() = runBlockingTest {
        every { firmwareFileManagerMock.getAvailableScannerFirmwareVersions() } returns SCANNER_VERSIONS_LOW
        coEvery { fingerprintApiClientMock.executeCall<List<DownloadableFirmwareVersion>>(any(), any()) } returns RESPONSE_MAP
        coEvery { fingerprintFileDownloaderMock.download(eq(CYPRESS_URL)) } returns CYPRESS_BIN
        coEvery { fingerprintFileDownloaderMock.download(eq(STM_URL)) } returns STM_BIN
        coEvery { fingerprintFileDownloaderMock.download(eq(UN20_URL)) } returns UN20_BIN

        firmwareFileUpdater.download()

        coVerify(Ordering.SEQUENCE) {
            fingerprintFileDownloaderMock.download(eq(CYPRESS_URL))
            fingerprintFileDownloaderMock.download(eq(STM_URL))
            fingerprintFileDownloaderMock.download(eq(UN20_URL))
        }
        coVerify(Ordering.ORDERED) {
            firmwareFileManagerMock.saveCypressFirmwareBytes(eq(CYPRESS_VERSION_HIGH), eq(CYPRESS_BIN))
            firmwareFileManagerMock.saveStmFirmwareBytes(eq(STM_VERSION_HIGH), eq(STM_BIN))
            firmwareFileManagerMock.saveUn20FirmwareBytes(eq(UN20_VERSION_HIGH), eq(UN20_BIN))
        }
    }

    @Test
    fun download_noVersionsAvailable_downloadsNoFiles() = runBlockingTest {
        every { firmwareFileManagerMock.getAvailableScannerFirmwareVersions() } returns SCANNER_VERSIONS_LOW
        coEvery { fingerprintApiClientMock.executeCall<List<DownloadableFirmwareVersion>>(any(), any()) } returns emptyList()

        firmwareFileUpdater.download()

        coVerify(exactly = 0) { fingerprintFileDownloaderMock.download(any()) }
        coVerify(exactly = 0) { firmwareFileManagerMock.saveCypressFirmwareBytes(any(), any()) }
        coVerify(exactly = 0) { firmwareFileManagerMock.saveStmFirmwareBytes(any(), any()) }
        coVerify(exactly = 0) { firmwareFileManagerMock.saveUn20FirmwareBytes(any(), any()) }
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

        private val CYPRESS_API_RESPONSE = ApiFirmwareVersionResponse(CYPRESS_NAME, CYPRESS_VERSION_HIGH.toString(), CYPRESS_URL)
        private val STM_API_RESPONSE = ApiFirmwareVersionResponse(STM_NAME, STM_VERSION_HIGH.toString(), STM_URL)
        private val UN20_API_RESPONSE = ApiFirmwareVersionResponse(UN20_NAME, UN20_VERSION_HIGH.toString(), UN20_URL)

        private val RESPONSE_MAP = mapOf(
            CYPRESS_NAME to CYPRESS_API_RESPONSE,
            STM_NAME to STM_API_RESPONSE,
            UN20_NAME to UN20_API_RESPONSE
        ).values.map { it.toDomain() }

        private val SCANNER_VERSIONS_LOW = ScannerFirmwareVersions(CYPRESS_VERSION_LOW, STM_VERSION_LOW, UN20_VERSION_LOW)

        private val CYPRESS_BIN = byteArrayOf(0x00, 0x01, 0x02)
        private val STM_BIN = byteArrayOf(0x03, 0x04)
        private val UN20_BIN = byteArrayOf(0x05, 0x06, 0x07, 0x08)
    }
}
