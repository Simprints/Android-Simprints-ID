package com.simprints.fingerprint.infra.scannermock.simulated.v2

import com.simprints.fingerprint.infra.scanner.v2.domain.Mode
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20AppVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.DigitalValue
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.LedState
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.SmileLedState
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.StmFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.CypressFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.UnifiedVersionInformation
import com.simprints.fingerprint.infra.scanner.v2.scanner.Scanner
import com.simprints.fingerprint.infra.scannermock.simulated.common.SimulatedScannerState

class SimulatedScannerStateV2(
    var mode: Mode = Mode.ROOT,
    var versionInfo: UnifiedVersionInformation =
        UnifiedVersionInformation(10L,
            CypressFirmwareVersion(12,13, 14, 15),
            StmFirmwareVersion(1, 2, 3, 4),
            Un20AppVersion(5, 6, 7, 8)),
    var isUn20On: Boolean = false,
    var isTriggerButtonActive: Boolean = true,
    var smileLedState: SmileLedState = SmileLedState(
        LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
        LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
        LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
        LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
        LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00)),
    var batteryPercentCharge: Int = 80,
    var batteryVoltageMilliVolts: Int = 4200,
    var batteryCurrentMilliAmps: Int = 20,
    var batteryTemperatureDeciKelvin: Int = 3000,
    var lastFingerCapturedDpi: Dpi = Scanner.DEFAULT_DPI
) : SimulatedScannerState()
