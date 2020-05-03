package com.simprints.fingerprint.scanner.tools

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import org.junit.Test

class ScannerGenerationDeterminerTest {

    private val scannerGenerationDeterminer = ScannerGenerationDeterminer()

    @Test
    fun serialNumberBelowLimit_returnsVero2() {
        assertThat(scannerGenerationDeterminer.determineScannerGenerationFromSerialNumber("SP099900"))
            .isEqualTo(ScannerGeneration.VERO_2)
    }

    @Test
    fun serialNumberBelowLimitButOnList_returnsVero1() {
        assertThat(scannerGenerationDeterminer.determineScannerGenerationFromSerialNumber("SP035700"))
            .isEqualTo(ScannerGeneration.VERO_1)
    }

    @Test
    fun serialNumberAboveLimit_returnsVero1() {
        assertThat(scannerGenerationDeterminer.determineScannerGenerationFromSerialNumber("SP199900"))
            .isEqualTo(ScannerGeneration.VERO_1)
    }
}
