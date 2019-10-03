package com.simprints.fingerprint.scanner.ui

import com.simprints.fingerprintscanner.v2.domain.message.vero.models.LedMode
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.LedState
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.SmileLedState

class ScannerUiHelper {

    fun goodScanLedState() = SmileLedState(
        LedState(LedMode.ON, 0x00, 0x00 ,0x04),
        LedState(LedMode.ON, 0x00, 0x00 ,0x04),
        LedState(LedMode.ON, 0x00, 0x00 ,0x04),
        LedState(LedMode.ON, 0x00, 0x00 ,0x04),
        LedState(LedMode.ON, 0x00, 0x00 ,0x04)
    )

    fun badScanLedState() = SmileLedState(
        LedState(LedMode.ON, 0x04, 0x00 ,0x00),
        LedState(LedMode.ON, 0x04, 0x00 ,0x00),
        LedState(LedMode.ON, 0x04, 0x00 ,0x00),
        LedState(LedMode.ON, 0x04, 0x00 ,0x00),
        LedState(LedMode.ON, 0x04, 0x00 ,0x00)
    )

    fun idleLedState() = SmileLedState(
        LedState(LedMode.OFF, 0x00, 0x00, 0x00),
        LedState(LedMode.OFF, 0x00, 0x00, 0x00),
        LedState(LedMode.OFF, 0x00, 0x00, 0x00),
        LedState(LedMode.OFF, 0x00, 0x00, 0x00),
        LedState(LedMode.OFF, 0x00, 0x00, 0x00)
    )
}
