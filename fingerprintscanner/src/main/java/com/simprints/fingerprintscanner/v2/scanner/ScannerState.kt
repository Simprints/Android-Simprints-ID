package com.simprints.fingerprintscanner.v2.scanner

import com.simprints.fingerprintscanner.v2.domain.message.vero.models.SmileLedState

data class ScannerState(
    var connected: Boolean?,
    var un20On: Boolean?,
    var triggerButtonActive: Boolean?,
    var smileLedState: SmileLedState?
)
