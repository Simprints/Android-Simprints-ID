package com.simprints.fingerprint.scanner.domain.versions

import com.google.common.truth.Truth
import com.simprints.fingerprint.scanner.data.FirmwareTestData
import com.simprints.fingerprint.scanner.data.FirmwareTestData.SCANNER_VERSIONS_HIGH
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.infra.config.domain.models.Vero2Configuration
import org.junit.Test

class ScannerHardwareRevisionsTest {

    @Test
    fun `test getDownloadableFirmwares with empty local ScannerFirmwareVersions `() {
        //Given
        val scannerHardwareRevisions = FirmwareTestData.RESPONSE_HARDWARE_REVISIONS_MAP
        val local = emptyMap<DownloadableFirmwareVersion.Chip, Set<String>>()
        // When
        val result =
            scannerHardwareRevisions.getAvailableVersionsForDownload(
                FirmwareTestData.HARDWARE_VERSION,
                local
            )
        // Then
        Truth.assertThat(result.size).isEqualTo(3)
    }

    @Test
    fun `test getDownloadableFirmwares with all firmware versions are available locally`() {

        //Given
        val scannerHardwareRevisions = FirmwareTestData.RESPONSE_HARDWARE_REVISIONS_MAP
        val local = SCANNER_VERSIONS_HIGH
        // When
        val result =
            scannerHardwareRevisions.getAvailableVersionsForDownload(
                FirmwareTestData.HARDWARE_VERSION,
                local
            )
        // Then
        Truth.assertThat(result.size).isEqualTo(0)
    }

    @Test
    fun `test getDownloadableFirmwares with empty ScannerHardwareRevisions`() {

        //Given
        val scannerHardwareRevisions = mapOf<String, Vero2Configuration.Vero2FirmwareVersions>()
        val local = SCANNER_VERSIONS_HIGH
        // When
        val result =
            scannerHardwareRevisions.getAvailableVersionsForDownload(
                FirmwareTestData.HARDWARE_VERSION,
                local
            )
        // Then
        Truth.assertThat(result.size).isEqualTo(0)
    }
}
