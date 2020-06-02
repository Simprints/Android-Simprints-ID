package com.simprints.fingerprint.scanner.data

import com.simprints.fingerprint.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.scanner.data.remote.ApiDownloadableFirmwareVersion
import com.simprints.fingerprint.scanner.data.remote.FirmwareRemoteDataSource
import com.simprints.fingerprint.scanner.data.remote.toDomain
import com.simprints.fingerprint.scanner.domain.versions.ChipFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
class FirmwareRepositoryTest {

    private val firmwareLocalDataSourceMock: FirmwareLocalDataSource = mockk(relaxUnitFun = true)
    private val firmwareRemoteDataSourceMock: FirmwareRemoteDataSource = mockk()

    private val firmwareFileUpdater = FirmwareRepository(firmwareRemoteDataSourceMock, firmwareLocalDataSourceMock)

    @Test
    fun updateStoredFirmwareFilesWithLatest_versionsAvailable_downloadsAndSavesFiles() = runBlockingTest {
        every { firmwareLocalDataSourceMock.getAvailableScannerFirmwareVersions() } returns SCANNER_VERSIONS_LOW
        coEvery { firmwareRemoteDataSourceMock.getDownloadableFirmwares(any()) } returns RESPONSE_MAP
        coEvery { firmwareRemoteDataSourceMock.downloadFile(eq(CYPRESS_URL)) } returns CYPRESS_BIN
        coEvery { firmwareRemoteDataSourceMock.downloadFile(eq(STM_URL)) } returns STM_BIN
        coEvery { firmwareRemoteDataSourceMock.downloadFile(eq(UN20_URL)) } returns UN20_BIN

        firmwareFileUpdater.updateStoredFirmwareFilesWithLatest()

        coVerify(Ordering.ORDERED) {
            firmwareRemoteDataSourceMock.downloadFile(eq(CYPRESS_URL))
            firmwareRemoteDataSourceMock.downloadFile(eq(STM_URL))
            firmwareRemoteDataSourceMock.downloadFile(eq(UN20_URL))
        }
        coVerify(Ordering.ORDERED) {
            firmwareLocalDataSourceMock.saveCypressFirmwareBytes(eq(CYPRESS_VERSION_HIGH), eq(CYPRESS_BIN))
            firmwareLocalDataSourceMock.saveStmFirmwareBytes(eq(STM_VERSION_HIGH), eq(STM_BIN))
            firmwareLocalDataSourceMock.saveUn20FirmwareBytes(eq(UN20_VERSION_HIGH), eq(UN20_BIN))
        }
    }

    @Test
    fun updateStoredFirmwareFilesWithLatest_noVersionsAvailable_downloadsNoFiles() = runBlockingTest {
        every { firmwareLocalDataSourceMock.getAvailableScannerFirmwareVersions() } returns SCANNER_VERSIONS_LOW
        coEvery { firmwareRemoteDataSourceMock.getDownloadableFirmwares(any()) } returns emptyList()

        firmwareFileUpdater.updateStoredFirmwareFilesWithLatest()

        coVerify(exactly = 0) { firmwareRemoteDataSourceMock.downloadFile(any()) }
        coVerify(exactly = 0) { firmwareLocalDataSourceMock.saveCypressFirmwareBytes(any(), any()) }
        coVerify(exactly = 0) { firmwareLocalDataSourceMock.saveStmFirmwareBytes(any(), any()) }
        coVerify(exactly = 0) { firmwareLocalDataSourceMock.saveUn20FirmwareBytes(any(), any()) }
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
        ).values.map { it.toDomain() }

        private val SCANNER_VERSIONS_LOW = ScannerFirmwareVersions(CYPRESS_VERSION_LOW, STM_VERSION_LOW, UN20_VERSION_LOW)

        private val CYPRESS_BIN = byteArrayOf(0x00, 0x01, 0x02)
        private val STM_BIN = byteArrayOf(0x03, 0x04)
        private val UN20_BIN = byteArrayOf(0x05, 0x06, 0x07, 0x08)
    }
}
