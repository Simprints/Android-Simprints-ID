package com.simprints.fingerprint.infra.scanner.domain.versions

import com.google.common.truth.Truth
import com.simprints.fingerprint.infra.scanner.data.FirmwareTestData
import com.simprints.fingerprint.infra.scanner.data.FirmwareTestData.HARDWARE_VERSION
import com.simprints.fingerprint.infra.scanner.data.FirmwareTestData.SCANNER_VERSIONS_HIGH
import com.simprints.fingerprint.infra.scanner.domain.ota.DownloadableFirmwareVersion
import org.junit.Test

class ScannerHardwareRevisionsTest {
    @Test
    fun `test getDownloadableFirmwares with empty local ScannerFirmwareVersions `() {
        // Given
        val scannerHardwareRevisions = FirmwareTestData.RESPONSE_HARDWARE_REVISIONS_MAP
        val local = emptyMap<DownloadableFirmwareVersion.Chip, Set<String>>()
        // When
        val result = scannerHardwareRevisions[HARDWARE_VERSION]?.getMissingVersionsToDownload(local)
        // Then
        Truth.assertThat(result?.size).isEqualTo(3)
    }

    @Test
    fun `test getDownloadableFirmwares with all firmware versions are available locally`() {
        // Given
        val scannerHardwareRevisions = FirmwareTestData.RESPONSE_HARDWARE_REVISIONS_MAP
        val local = SCANNER_VERSIONS_HIGH
        // When
        val result = scannerHardwareRevisions[HARDWARE_VERSION]?.getMissingVersionsToDownload(local)
        // Then
        Truth.assertThat(result?.size).isEqualTo(0)
    }
}
