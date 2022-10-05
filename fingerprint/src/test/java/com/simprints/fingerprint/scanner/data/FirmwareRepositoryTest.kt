package com.simprints.fingerprint.scanner.data

import com.simprints.fingerprint.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.scanner.data.remote.FirmwareRemoteDataSource
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.getAvailableVersionsForDownload
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.Vero2Configuration
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FirmwareRepositoryTest {

    private val firmwareLocalDataSourceMock: FirmwareLocalDataSource = mockk(relaxUnitFun = true)
    private val firmwareRemoteDataSourceMock: FirmwareRemoteDataSource = mockk()
    private val vero2Configuration = mockk<Vero2Configuration>()
    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { fingerprint } returns mockk {
                every { vero2 } returns vero2Configuration
            }
        }
    }

    private val firmwareFileUpdater =
        FirmwareRepository(firmwareRemoteDataSourceMock, firmwareLocalDataSourceMock, configManager)

    @Before
    fun setup() {
        every { vero2Configuration.firmwareVersions } returns mapOf(
            HARDWARE_VERSION to Vero2Configuration.Vero2FirmwareVersions(
                CYPRESS_VERSION_HIGH,
                STM_VERSION_HIGH,
                UN20_VERSION_HIGH
            )
        )
    }

    @Test
    fun updateStoredFirmwareFilesWithLatest_versionsAvailable_downloadsAndSavesFiles() =
        runTest {
            coEvery { firmwareRemoteDataSourceMock.downloadFirmware(any()) } returns CYPRESS_BIN
            every {
                firmwareLocalDataSourceMock.getAvailableScannerFirmwareVersions()
            } returns SCANNER_VERSIONS_LOW
            val availableForDownload = RESPONSE_MAP.getAvailableVersionsForDownload(
                HARDWARE_VERSION,
                emptyMap()
            )
            coEvery {
                firmwareRemoteDataSourceMock.getDownloadableFirmwares(
                    any(),
                    any()
                )
            } returns availableForDownload

            firmwareFileUpdater.updateStoredFirmwareFilesWithLatest()

            coVerify(exactly = 3) {
                firmwareRemoteDataSourceMock.downloadFirmware(any())
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
        runTest {
            every {
                firmwareLocalDataSourceMock.getAvailableScannerFirmwareVersions()
            } returns SCANNER_VERSIONS_LOW
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

    @Test
    fun updateStoredFirmwareFilesWithLatest_onlyOneVersionsAvailable_downloadsOtherFiles() =
        runTest {
            every {
                firmwareLocalDataSourceMock.getAvailableScannerFirmwareVersions()
            } returns SCANNER_VERSIONS_LOW_UN20_HIGH
            val availableForDownload = RESPONSE_MAP.getAvailableVersionsForDownload(
                HARDWARE_VERSION,
                SCANNER_VERSIONS_HIGH
            )
            coEvery {
                firmwareRemoteDataSourceMock.getDownloadableFirmwares(
                    any(),
                    any()
                )
            } returns availableForDownload

            firmwareFileUpdater.updateStoredFirmwareFilesWithLatest()

            coVerify(exactly = 0) { firmwareRemoteDataSourceMock.downloadFirmware(any()) }

            coVerify(exactly = 0) {
                firmwareLocalDataSourceMock.saveUn20FirmwareBytes(
                    any(),
                    any()
                )
            }
        }

    @Test
    fun `test cleanUpOldFirmwareFiles should remove all low version firmwares`() = runTest {
        //Given
        every {
            firmwareLocalDataSourceMock.getAvailableScannerFirmwareVersions()
        } returns SCANNER_VERSIONS_LOW_AND_HIGH
        firmwareLocalDataSourceMock.apply {
            every { deleteCypressFirmware(any()) } returns true
            every { deleteStmFirmware(any()) } returns true
            every { deleteUn20Firmware(any()) } returns true
        }
        // When
        firmwareFileUpdater.cleanUpOldFirmwareFiles()
        // Then
        verify {
            firmwareLocalDataSourceMock.deleteCypressFirmware(CYPRESS_VERSION_LOW)
            firmwareLocalDataSourceMock.deleteStmFirmware(STM_VERSION_LOW)
            firmwareLocalDataSourceMock.deleteUn20Firmware(UN20_VERSION_LOW)
        }
    }

    @Test
    fun `test cleanUpOldFirmwareFiles should remove nothing if all firmware versions are up to date`() =
        runTest {
            //Given
            every {
                firmwareLocalDataSourceMock.getAvailableScannerFirmwareVersions()
            } returns SCANNER_VERSIONS_HIGH

            // When
            firmwareFileUpdater.cleanUpOldFirmwareFiles()
            // Then
            verify(exactly = 0) {
                firmwareLocalDataSourceMock.deleteCypressFirmware(CYPRESS_VERSION_LOW)
                firmwareLocalDataSourceMock.deleteStmFirmware(CYPRESS_VERSION_LOW)
                firmwareLocalDataSourceMock.deleteUn20Firmware(CYPRESS_VERSION_LOW)
            }
        }

    companion object {
        private const val HARDWARE_VERSION = "E-1"
        private const val CYPRESS_VERSION_LOW = "1.E-1.0"
        private const val CYPRESS_VERSION_HIGH = "1.E-1.1"
        private const val STM_VERSION_LOW = "1.E-1.1"
        private const val STM_VERSION_HIGH = "1.E-1.2"
        private const val UN20_VERSION_LOW = "1.E-1.2"
        private const val UN20_VERSION_HIGH = "1.E-1.3"

        private val RESPONSE_MAP = mapOf(
            HARDWARE_VERSION to Vero2Configuration.Vero2FirmwareVersions(
                CYPRESS_VERSION_HIGH,
                STM_VERSION_HIGH,
                UN20_VERSION_HIGH
            )
        )
        private val SCANNER_VERSIONS_LOW = mapOf(
            DownloadableFirmwareVersion.Chip.CYPRESS to setOf(CYPRESS_VERSION_LOW),
            DownloadableFirmwareVersion.Chip.STM to setOf(STM_VERSION_LOW),
            DownloadableFirmwareVersion.Chip.UN20 to setOf(UN20_VERSION_LOW),
        )
        private val SCANNER_VERSIONS_LOW_AND_HIGH = mapOf(
            DownloadableFirmwareVersion.Chip.CYPRESS to setOf(
                CYPRESS_VERSION_LOW,
                CYPRESS_VERSION_HIGH
            ),
            DownloadableFirmwareVersion.Chip.STM to setOf(STM_VERSION_LOW, STM_VERSION_HIGH),
            DownloadableFirmwareVersion.Chip.UN20 to setOf(UN20_VERSION_LOW, UN20_VERSION_HIGH),
        )
        private val SCANNER_VERSIONS_HIGH = mapOf(
            DownloadableFirmwareVersion.Chip.CYPRESS to setOf(CYPRESS_VERSION_HIGH),
            DownloadableFirmwareVersion.Chip.STM to setOf(STM_VERSION_HIGH),
            DownloadableFirmwareVersion.Chip.UN20 to setOf(UN20_VERSION_HIGH),
        )
        private val SCANNER_VERSIONS_LOW_UN20_HIGH = mapOf(
            DownloadableFirmwareVersion.Chip.CYPRESS to setOf(CYPRESS_VERSION_LOW),
            DownloadableFirmwareVersion.Chip.STM to setOf(STM_VERSION_LOW),
            DownloadableFirmwareVersion.Chip.UN20 to setOf(UN20_VERSION_HIGH),
        )

        private val CYPRESS_BIN = byteArrayOf(0x00, 0x01, 0x02)
    }
}
