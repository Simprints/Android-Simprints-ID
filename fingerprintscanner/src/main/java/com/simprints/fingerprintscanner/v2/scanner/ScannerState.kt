package com.simprints.fingerprintscanner.v2.scanner

import com.simprints.fingerprintscanner.v2.domain.Mode
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.SmileLedState

data class ScannerState(
    var connected: Boolean?,
    var mode: Mode?,
    var un20On: Boolean?,
    var triggerButtonActive: Boolean?,
    var smileLedState: SmileLedState?,
    var batteryPercentCharge: Int?,
    var batteryVoltageMilliVolts: Int?,
    var batteryCurrentMilliAmps: Int?,
    var batteryTemperatureDeciKelvin: Int?
)

fun disconnectedScannerState() =
    ScannerState(
        connected = false,
        mode = null,
        un20On = null,
        triggerButtonActive = null,
        smileLedState = null,
        batteryPercentCharge = null,
        batteryVoltageMilliVolts = null,
        batteryCurrentMilliAmps = null,
        batteryTemperatureDeciKelvin = null)
