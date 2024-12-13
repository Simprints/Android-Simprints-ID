package com.simprints.fingerprint.infra.scanner.tools

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.FingerprintConfiguration
import org.junit.Test

class ScannerGenerationDeterminerTest {
    private val scannerGenerationDeterminer = ScannerGenerationDeterminer()

    @Test
    fun serialNumberBelowLimit_returnsVero2() {
        assertThat(scannerGenerationDeterminer.determineScannerGenerationFromSerialNumber("SP099900"))
            .isEqualTo(FingerprintConfiguration.VeroGeneration.VERO_2)
    }

    @Test
    fun serialNumberBelowLimitButOnList_returnsVero1() {
        assertThat(scannerGenerationDeterminer.determineScannerGenerationFromSerialNumber("SP035700"))
            .isEqualTo(FingerprintConfiguration.VeroGeneration.VERO_1)
    }

    @Test
    fun serialNumberAboveLimit_returnsVero1() {
        assertThat(scannerGenerationDeterminer.determineScannerGenerationFromSerialNumber("SP199900"))
            .isEqualTo(FingerprintConfiguration.VeroGeneration.VERO_1)
    }
}
