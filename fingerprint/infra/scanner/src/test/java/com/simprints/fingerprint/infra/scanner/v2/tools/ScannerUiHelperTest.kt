package com.simprints.fingerprint.infra.scanner.v2.tools

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.SmileLedState
import com.simprints.fingerprint.infra.scanner.v2.tools.ScannerUiHelper.Companion.G
import com.simprints.fingerprint.infra.scanner.v2.tools.ScannerUiHelper.Companion.R
import com.simprints.fingerprint.infra.scanner.v2.tools.ScannerUiHelper.Companion.X
import org.junit.Test

class ScannerUiHelperTest {
    private val scannerUiHelper = ScannerUiHelper()

    @Test
    fun goodScanLedState() {
        val result = scannerUiHelper.goodScanLedState()
        assertThat(result).isEqualTo(SmileLedState(G, G, G, G, G))
    }

    @Test
    fun badScanLedState() {
        val result = scannerUiHelper.badScanLedState()
        assertThat(result).isEqualTo(SmileLedState(R, R, R, R, R))
    }

    @Test
    fun turnedOffState() {
        val result = scannerUiHelper.turnedOffState()
        assertThat(result).isEqualTo(SmileLedState(X, X, X, X, X))
    }

    @Test
    fun deduceLedStateFromQualityForLiveFeedback() {
        var quality = 90
        var result = scannerUiHelper.deduceLedStateFromQualityForLiveFeedback(quality)
        assertThat(result).isEqualTo(SmileLedState(G, G, G, G, G))

        quality = 80
        result = scannerUiHelper.deduceLedStateFromQualityForLiveFeedback(quality)
        assertThat(result).isEqualTo(SmileLedState(G, G, G, G, X))

        quality = 70
        result = scannerUiHelper.deduceLedStateFromQualityForLiveFeedback(quality)
        assertThat(result).isEqualTo(SmileLedState(G, G, G, X, X))

        quality = 50
        result = scannerUiHelper.deduceLedStateFromQualityForLiveFeedback(quality)
        assertThat(result).isEqualTo(SmileLedState(G, G, X, X, X))

        quality = 30
        result = scannerUiHelper.deduceLedStateFromQualityForLiveFeedback(quality)
        assertThat(result).isEqualTo(SmileLedState(G, X, X, X, X))
    }
}
