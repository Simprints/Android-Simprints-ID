package com.simprints.fingerprint.infra.scanner.data

import com.simprints.fingerprint.infra.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.infra.scanner.data.remote.FirmwareRemoteDataSource
import com.simprints.fingerprint.infra.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.infra.config.store.models.Vero2Configuration
import com.simprints.infra.config.store.models.Vero2Configuration.Vero2FirmwareVersions
import com.simprints.fingerprint.infra.scanner.domain.versions.getMissingVersionsToDownload
import com.simprints.infra.config.sync.ConfigManager
import io.mockk.MockKAnnotations
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FirmwareRepositoryTest {

    @MockK(relaxUnitFun = true)
    private lateinit var firmwareLocalDataSourceMock: FirmwareLocalDataSource

    @MockK(relaxed = true)
    private lateinit var firmwareRemoteDataSourceMock: FirmwareRemoteDataSource

    @MockK
    private lateinit var vero2Configuration: Vero2Configuration

    @MockK
    private lateinit var configManager: ConfigManager

    private lateinit var firmwareRepository: FirmwareRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery {
            configManager.getProjectConfiguration().fingerprint?.secugenSimMatcher?.vero2
        } returns vero2Configuration

        every { vero2Configuration.firmwareVersions } returns mapOf(
            HARDWARE_VERSION to Vero2FirmwareVersions(
                CYPRESS_VERSION_HIGH,
                STM_VERSION_HIGH,
                UN20_VERSION_HIGH
            )
        )

        firmwareRepository = FirmwareRepository(
            firmwareRemoteDataSourceMock,
            firmwareLocalDataSourceMock,
            configManager
        )

        mockkStatic("com.simprints.fingerprint.infra.scanner.domain.versions.ScannerHardwareRevisionsKt")
    }

    @Test
    fun `updateStoredFirmwareFilesWithLatest downloads the latest file versions when available`() =
        runTest {
            coEvery { firmwareRemoteDataSourceMock.downloadFirmware(any()) } returns CYPRESS_BIN
            coEvery {
                firmwareLocalDataSourceMock.getAvailableScannerFirmwareVersions()
            } returns SCANNER_VERSIONS_LOW

            firmwareRepository.updateStoredFirmwareFilesWithLatest()

            coVerify(exactly = 3) {
                firmwareRemoteDataSourceMock.downloadFirmware(any())
            }
            coVerify(Ordering.ORDERED) {
                firmwareLocalDataSourceMock.saveCypressFirmwareBytes(any(), any())
                firmwareLocalDataSourceMock.saveStmFirmwareBytes(any(), any())
                firmwareLocalDataSourceMock.saveUn20FirmwareBytes(any(), any())
            }
        }

    @Test
    fun `updateStoredFirmwareFilesWithLatest downloads no files when no versions available`() =
        runTest {
            coEvery {
                firmwareLocalDataSourceMock.getAvailableScannerFirmwareVersions()
            } returns SCANNER_VERSIONS_LOW
            every {
                any<Vero2FirmwareVersions>().getMissingVersionsToDownload(any())
            } returns emptyList()

            firmwareRepository.updateStoredFirmwareFilesWithLatest()

            coVerify(exactly = 0) { firmwareRemoteDataSourceMock.downloadFirmware(any()) }
            coVerify(exactly = 0) { firmwareLocalDataSourceMock.saveCypressFirmwareBytes(any(), any()) }
            coVerify(exactly = 0) { firmwareLocalDataSourceMock.saveStmFirmwareBytes(any(), any()) }
            coVerify(exactly = 0) { firmwareLocalDataSourceMock.saveUn20FirmwareBytes(any(), any()) }
        }

    @Test
    fun `updateStoredFirmwareFilesWithLatest downloads other files when only one versions available`() =
        runTest {
            coEvery { firmwareRemoteDataSourceMock.downloadFirmware(any()) } returns CYPRESS_BIN
            coEvery {
                firmwareLocalDataSourceMock.getAvailableScannerFirmwareVersions()
            } returns SCANNER_VERSIONS_LOW_UN20_HIGH

            firmwareRepository.updateStoredFirmwareFilesWithLatest()

            coVerify(exactly = 2) { firmwareRemoteDataSourceMock.downloadFirmware(any()) }
            coVerify(exactly = 1) { firmwareLocalDataSourceMock.saveCypressFirmwareBytes(any(), any()) }
            coVerify(exactly = 1) { firmwareLocalDataSourceMock.saveStmFirmwareBytes(any(), any()) }
            coVerify(exactly = 0) { firmwareLocalDataSourceMock.saveUn20FirmwareBytes(any(), any()) }
        }

    @Test
    fun `cleanUpOldFirmwareFiles should remove all low version firmwares`() = runTest {
        //Given
        coEvery {
            firmwareLocalDataSourceMock.getAvailableScannerFirmwareVersions()
        } returns SCANNER_VERSIONS_LOW_AND_HIGH
        firmwareLocalDataSourceMock.apply {
            coEvery { deleteCypressFirmware(any()) } returns true
            coEvery { deleteStmFirmware(any()) } returns true
            coEvery { deleteUn20Firmware(any()) } returns true
        }
        // When
        firmwareRepository.cleanUpOldFirmwareFiles()
        // Then
        coVerify {
            firmwareLocalDataSourceMock.deleteCypressFirmware(CYPRESS_VERSION_LOW)
            firmwareLocalDataSourceMock.deleteStmFirmware(STM_VERSION_LOW)
            firmwareLocalDataSourceMock.deleteUn20Firmware(UN20_VERSION_LOW)
        }
    }

    @Test
    fun `cleanUpOldFirmwareFiles should remove nothing if all firmware versions are up to date`() =
        runTest {
            //Given
            coEvery {
                firmwareLocalDataSourceMock.getAvailableScannerFirmwareVersions()
            } returns SCANNER_VERSIONS_HIGH

            // When
            firmwareRepository.cleanUpOldFirmwareFiles()
            // Then
            coVerify(exactly = 0) {
                firmwareLocalDataSourceMock.deleteCypressFirmware(CYPRESS_VERSION_LOW)
                firmwareLocalDataSourceMock.deleteStmFirmware(CYPRESS_VERSION_LOW)
                firmwareLocalDataSourceMock.deleteUn20Firmware(CYPRESS_VERSION_LOW)
            }
        }

    @Test
    fun `deleteAllFirmwareFiles should delete all files`() = runTest {
        firmwareRepository.deleteAllFirmwareFiles()

        coVerify { firmwareLocalDataSourceMock.deleteAllFirmware() }
    }

    companion object {

        private const val HARDWARE_VERSION = "E-1"
        private const val CYPRESS_VERSION_LOW = "1.E-1.0"
        private const val CYPRESS_VERSION_HIGH = "1.E-1.1"
        private const val STM_VERSION_LOW = "1.E-1.1"
        private const val STM_VERSION_HIGH = "1.E-1.2"
        private const val UN20_VERSION_LOW = "1.E-1.2"
        private const val UN20_VERSION_HIGH = "1.E-1.3"

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
