package com.simprints.fingerprint.infra.scanner.v2.scanner

import com.simprints.fingerprint.infra.scanner.v2.domain.Mode
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.SmileLedState

/**
 * Class for storing the state of the Scanner, as received from communicating from the scanner.
 * Null values indicate unknown state.
 */
data class ScannerState(
    var connected: Boolean,
    var mode: Mode?,
    var un20On: Boolean?,
    var triggerButtonActive: Boolean?,
    var smileLedState: SmileLedState?,
    var batteryPercentCharge: Int?,
    var batteryVoltageMilliVolts: Int?,
    var batteryCurrentMilliAmps: Int?,
    var batteryTemperatureDeciKelvin: Int?,
    var scanLedState: Boolean?, // FALSE = Default
)

fun disconnectedScannerState() = ScannerState(
    connected = false,
    mode = null,
    un20On = null,
    triggerButtonActive = null,
    smileLedState = null,
    batteryPercentCharge = null,
    batteryVoltageMilliVolts = null,
    batteryCurrentMilliAmps = null,
    batteryTemperatureDeciKelvin = null,
    scanLedState = null,
)
