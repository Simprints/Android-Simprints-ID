package com.simprints.fingerprint.scanner.domain.versions

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.testtools.common.syntax.assertThrows
import org.junit.Test

class ScannerVersionTest {

    @Test
    fun computeCombinedFirmwareVersion_yieldsCorrectResult() {
        val chipFirmwareVersion = ChipFirmwareVersion(5, 9)
        assertThat(chipFirmwareVersion.combined()).isEqualTo(327689)
    }

    @Test
    fun computeCombinedApiVersion_yieldsCorrectResult() {
        val chipApiVersion = ChipApiVersion(6, 1)
        assertThat(chipApiVersion.combined()).isEqualTo(393217)
    }

    @Test
    fun computeCombinedScannerFirmwareVersions_yieldsCorrectResult() {
        val scannerFirmwareVersions = ScannerFirmwareVersions(
            ChipFirmwareVersion(1, 2), ChipFirmwareVersion(3, 4), ChipFirmwareVersion(5, 6)
        )
        assertThat(scannerFirmwareVersions.combined()).isEqualTo(589836)
    }

    @Test
    fun computeCombinedScannerApiVersions_yieldsCorrectResult() {
        val scannerApiVersions = ScannerApiVersions(
            ChipApiVersion(7, 8), ChipApiVersion(9, 10), ChipApiVersion(11, 12)
        )
        assertThat(scannerApiVersions.combined()).isEqualTo(1769502)
    }

    @Test
    fun computeMasterVersion_forVero2_yieldsCorrectResult() {
        val scannerVersion = ScannerVersion(ScannerGeneration.VERO_2,
            ScannerFirmwareVersions(
                ChipFirmwareVersion(3, 4), ChipFirmwareVersion(7, 8), ChipFirmwareVersion(11, 12)
            ),
            ScannerApiVersions(
                ChipApiVersion(1, 2), ChipApiVersion(5, 6), ChipApiVersion(9, 10)
            )

        )
        assertThat(scannerVersion.computeMasterVersion()).isEqualTo(4222201961447448L)
    }

    @Test
    fun computeMasterVersion_forVero2WithUnknownValues_throwsException() {
        val scannerVersion = ScannerVersion(ScannerGeneration.VERO_2,
            ScannerFirmwareVersions(
                ChipFirmwareVersion(3, 4), ChipFirmwareVersion(7, 8), ChipFirmwareVersion(11, 12)
            ),
            ScannerApiVersions(
                ChipApiVersion(1, 2), ChipApiVersion.UNKNOWN, ChipApiVersion(9, 10)
            )

        )
        assertThrows<IllegalArgumentException> { scannerVersion.computeMasterVersion() }
    }
}
