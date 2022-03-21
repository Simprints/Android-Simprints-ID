package com.simprints.fingerprint.scanner.domain.versions

import com.google.common.truth.Truth
import com.simprints.fingerprint.scanner.data.FirmwareTestData
import org.junit.Before
import org.junit.Test

class ScannerHardwareRevisionsTest {

    @Before
    fun setUp() {
    }

    @Test
    fun `test getDownloadableFirmwares with empty local ScannerFirmwareVersions `() {

        //Given
        val scannerHardwareRevisions = FirmwareTestData.RESPONSE_HARDWARE_REVISIONS_MAP
        val local = ScannerFirmwareVersions.UNKNOWN
        // When
        val result =
            scannerHardwareRevisions.availableForDownload(FirmwareTestData.HARDWARE_VERSION, local)
        // Then
        Truth.assertThat(result.size).isEqualTo(3)
    }
    @Test
    fun `test getDownloadableFirmwares with all firmware versions are available locally`() {

        //Given
        val scannerHardwareRevisions = FirmwareTestData.RESPONSE_HARDWARE_REVISIONS_MAP
        val local = FirmwareTestData.scannerFirmwareVersions
        // When
        val result =
            scannerHardwareRevisions.availableForDownload(FirmwareTestData.HARDWARE_VERSION, local)
        // Then
        Truth.assertThat(result.size).isEqualTo(0)
    }
    @Test
    fun `test getDownloadableFirmwares with empty ScannerHardwareRevisions`() {

        //Given
        val scannerHardwareRevisions = ScannerHardwareRevisions()
        val local = FirmwareTestData.scannerFirmwareVersions
        // When
        val result =
            scannerHardwareRevisions.availableForDownload(FirmwareTestData.HARDWARE_VERSION, local)
        // Then
        Truth.assertThat(result.size).isEqualTo(0)
    }
}
