package com.simprints.fingerprintscanner.v2.scanner

import com.simprints.fingerprintscanner.v2.domain.message.vero.models.FirmwareVersion

data class ScannerState(
    var connected: Boolean?,
    var firmwareVersion: FirmwareVersion?,
    var un20On: Boolean?,
    var triggerButtonActive: Boolean?
)
