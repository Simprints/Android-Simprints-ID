package com.simprints.fingerprint.infra.scanner.v2.tools

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.DigitalValue
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.LedState
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.SmileLedState
import javax.inject.Inject

/**
 * Helper class for determining LED colours for Vero 2
 */
class ScannerUiHelper @Inject constructor() {
    fun goodScanLedState() = SmileLedState(G, G, G, G, G)

    fun badScanLedState() = SmileLedState(R, R, R, R, R)

    fun turnedOffState() = SmileLedState(X, X, X, X, X)

    fun whiteFlashingLedState() = SmileLedState(W, W, W, W, W)

    fun deduceLedStateFromQualityForLiveFeedback(quality: Int) = when (quality) {
        in QUALITY[0] -> SmileLedState(G, X, X, X, X)
        in QUALITY[1] -> SmileLedState(G, G, X, X, X)
        in QUALITY[2] -> SmileLedState(G, G, G, X, X)
        in QUALITY[3] -> SmileLedState(G, G, G, G, X)
        in QUALITY[4] -> SmileLedState(G, G, G, G, G)
        else -> turnedOffState()
    }

    companion object {
        val QUALITY = listOf(
            0 until 40,
            40 until 60,
            60 until 75,
            75 until 85,
            85 until 101,
        )

        val X = LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00) // off
        val G = LedState(DigitalValue.FALSE, 0x00, 0x08, 0x00) // green
        val R = LedState(DigitalValue.FALSE, 0x08, 0x00, 0x00) // red
        val W = LedState(DigitalValue.TRUE, 0x08, 0x08, 0x08) // white
    }
}
