package com.simprints.fingerprint.scanner.ui

import com.simprints.fingerprintscanner.v2.domain.message.vero.models.DigitalValue
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.LedState
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.SmileLedState

class ScannerUiHelper {

    fun goodScanLedState() = SmileLedState(
        LedState(DigitalValue.FALSE, 0x00, 0x00 ,0x04),
        LedState(DigitalValue.FALSE, 0x00, 0x00 ,0x04),
        LedState(DigitalValue.FALSE, 0x00, 0x00 ,0x04),
        LedState(DigitalValue.FALSE, 0x00, 0x00 ,0x04),
        LedState(DigitalValue.FALSE, 0x00, 0x00 ,0x04)
    )

    fun badScanLedState() = SmileLedState(
        LedState(DigitalValue.FALSE, 0x04, 0x00 ,0x00),
        LedState(DigitalValue.FALSE, 0x04, 0x00 ,0x00),
        LedState(DigitalValue.FALSE, 0x04, 0x00 ,0x00),
        LedState(DigitalValue.FALSE, 0x04, 0x00 ,0x00),
        LedState(DigitalValue.FALSE, 0x04, 0x00 ,0x00)
    )

    fun idleLedState() = SmileLedState(
        LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
        LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
        LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
        LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
        LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00)
    )
}
