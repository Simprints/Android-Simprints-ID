package com.simprints.fingerprint.scanner.data

import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.scanner.data.remote.FirmwareRemoteDataSource
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerRevisions
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class FirmwareRepositoryTest {

    private val firmwareLocalDataSourceMock: FirmwareLocalDataSource = mockk(relaxUnitFun = true)
    private val firmwareRemoteDataSourceMock: FirmwareRemoteDataSource = mockk()
    private val fingerprintPreferencesMock: FingerprintPreferencesManager = mockk()

    private val firmwareFileUpdater =
        FirmwareRepository(
            firmwareRemoteDataSourceMock, firmwareLocalDataSourceMock,
            fingerprintPreferencesMock
        )

    @Before
    fun setup() {
        every { fingerprintPreferencesMock.scannerRevisions } returns RESPONSE_MAP
    }

    @Test
    fun updateStoredFirmwareFilesWithLatest_versionsAvailable_downloadsAndSavesFiles() =
        runBlockingTest {
            coEvery { firmwareRemoteDataSourceMock.downloadFirmware(any())} returns CYPRESS_BIN
            val availableForDownload = RESPONSE_MAP.availableForDownload(
                HARDWARE_VERSION,
                ScannerFirmwareVersions.UNKNOWN
            )
            every { firmwareLocalDataSourceMock.getAvailableScannerFirmwareVersions(HARDWARE_VERSION) } returns SCANNER_VERSIONS_LOW
            coEvery {
                firmwareRemoteDataSourceMock.getDownloadableFirmwares(
                    any(),
                    any()
                )
            } returns availableForDownload

            firmwareFileUpdater.updateStoredFirmwareFilesWithLatest()

            coVerify(exactly = 3) {
                firmwareRemoteDataSourceMock.downloadFirmware(any())
                // Will add better verification later
            }
            coVerify(Ordering.ORDERED) {
                firmwareLocalDataSourceMock.saveCypressFirmwareBytes(
                    any(), any()
                )
                firmwareLocalDataSourceMock.saveStmFirmwareBytes(
                    any(), any()
                )
                firmwareLocalDataSourceMock.saveUn20FirmwareBytes(
                any(), any()
            )

            }
        }

    @Test
    fun updateStoredFirmwareFilesWithLatest_noVersionsAvailable_downloadsNoFiles() =
        runBlockingTest {
            every { firmwareLocalDataSourceMock.getAvailableScannerFirmwareVersions(HARDWARE_VERSION) } returns SCANNER_VERSIONS_LOW
            coEvery {
                firmwareRemoteDataSourceMock.getDownloadableFirmwares(
                    any(),
                    any()
                )
            } returns emptyList()

            firmwareFileUpdater.updateStoredFirmwareFilesWithLatest()

            coVerify(exactly = 0) { firmwareRemoteDataSourceMock.downloadFirmware(any()) }
            coVerify(exactly = 0) {
                firmwareLocalDataSourceMock.saveCypressFirmwareBytes(
                    any(),
                    any()
                )
            }
            coVerify(exactly = 0) { firmwareLocalDataSourceMock.saveStmFirmwareBytes(any(), any()) }
            coVerify(exactly = 0) {
                firmwareLocalDataSourceMock.saveUn20FirmwareBytes(
                    any(),
                    any()
                )
            }
        }

    companion object {
        private const val HARDWARE_VERSION = "E-1"
// I will use this variables in more tests soon
        private const val CYPRESS_NAME = "cypress"
        private val CYPRESS_VERSION_LOW = "1.E-1.0"
        private val CYPRESS_VERSION_HIGH = "1.E-1.1"
        private const val CYPRESS_URL = "cypress_url.com"

        private const val STM_NAME = "stm"
        private val STM_VERSION_LOW = "1.E-1.1"
        private val STM_VERSION_HIGH = "1.E-1.2"
        private const val STM_URL = "stm_url.com"

        private const val UN20_NAME = "un20"
        private val UN20_VERSION_LOW = "1.E-1.2"
        private val UN20_VERSION_HIGH = "1.E-1.3"
        private const val UN20_URL = "un20_url.com"


        private val scannerFirmwareVersions =
            ScannerFirmwareVersions(
                cypress = CYPRESS_VERSION_HIGH,
                stm = STM_VERSION_HIGH,
                un20 = UN20_VERSION_HIGH,
            )
        private val RESPONSE_MAP = ScannerRevisions().apply {
            put(HARDWARE_VERSION, scannerFirmwareVersions)
        }


        private val SCANNER_VERSIONS_LOW =
            ScannerFirmwareVersions(CYPRESS_VERSION_LOW, STM_VERSION_LOW, UN20_VERSION_LOW)

        private val CYPRESS_BIN = byteArrayOf(0x00, 0x01, 0x02)
        private val STM_BIN = byteArrayOf(0x03, 0x04)
        private val UN20_BIN = byteArrayOf(0x05, 0x06, 0x07, 0x08)
    }
}
