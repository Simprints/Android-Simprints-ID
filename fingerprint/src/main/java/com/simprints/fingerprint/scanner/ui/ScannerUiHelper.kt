package com.simprints.fingerprint.scanner.ui

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.DigitalValue
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.LedState
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.SmileLedState

/**
 * Helper class for determining LED colours for Vero 2
 */
class ScannerUiHelper {

    fun goodScanLedState() = SmileLedState(G, G, G, G, G)

    fun badScanLedState() = SmileLedState(R, R, R, R, R)

    fun idleLedState() = SmileLedState(X, X, X, X, X)

    fun deduceLedStateFromQualityForLiveFeedback(quality : Int) =
        when (quality) {
            in QUALITY[0] -> SmileLedState(G, X, X, X, X)
            in QUALITY[1] -> SmileLedState(G, G, X, X, X)
            in QUALITY[2] -> SmileLedState(G, G, G, X, X)
            in QUALITY[3] -> SmileLedState(G, G, G, G, X)
            in QUALITY[4] -> SmileLedState(G, G, G, G, G)
            else -> idleLedState()
        }

    companion object {
        val QUALITY = listOf<IntRange>(
            0 until 40,
            40 until 60,
            60 until 75,
            75 until 85,
            85 until 101
        )

        val X = LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00) // off
        val G = LedState(DigitalValue.FALSE, 0x00, 0x08, 0x00) // green
        val R = LedState(DigitalValue.FALSE, 0x08, 0x00 ,0x00) // red

    }
}
