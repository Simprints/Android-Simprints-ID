package com.simprints.fingerprint.scanner.ui

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.DigitalValue
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.LedState
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.SmileLedState

class ScannerUiHelper {

    fun goodScanLedState() = SmileLedState(
        LedState(DigitalValue.FALSE, 0x00, 0x08 ,0x00),
        LedState(DigitalValue.FALSE, 0x00, 0x08 ,0x00),
        LedState(DigitalValue.FALSE, 0x00, 0x08 ,0x00),
        LedState(DigitalValue.FALSE, 0x00, 0x08 ,0x00),
        LedState(DigitalValue.FALSE, 0x00, 0x08 ,0x00)
    )

    fun badScanLedState() = SmileLedState(
        LedState(DigitalValue.FALSE, 0x08, 0x00 ,0x00),
        LedState(DigitalValue.FALSE, 0x08, 0x00 ,0x00),
        LedState(DigitalValue.FALSE, 0x08, 0x00 ,0x00),
        LedState(DigitalValue.FALSE, 0x08, 0x00 ,0x00),
        LedState(DigitalValue.FALSE, 0x08, 0x00 ,0x00)
    )

    fun idleLedState() = SmileLedState(
        LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
        LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
        LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
        LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
        LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00)
    )

    fun deduceLedStateFromQualityForLiveFeedback(quality : Int) =
        when (quality) {
            in 0 until 40 -> SmileLedState(
                LedState(DigitalValue.FALSE, 0x00, 0x08 ,0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00)
            )
            in 40 until 60 -> SmileLedState(
                LedState(DigitalValue.FALSE, 0x00, 0x08 ,0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x08, 0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00)
            )
            in 60 until 75 -> SmileLedState(
                LedState(DigitalValue.FALSE, 0x00, 0x08 ,0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x08, 0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x08, 0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00)
            )
            in 75 until 85 -> SmileLedState(
                LedState(DigitalValue.FALSE, 0x00, 0x08 ,0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x08, 0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x08, 0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x08, 0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00)
            )
            in 85 until 101 -> SmileLedState(
                LedState(DigitalValue.FALSE, 0x00, 0x08 ,0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x08, 0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x08, 0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x08, 0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x08, 0x00)
            )
            else -> idleLedState()
        }
}
