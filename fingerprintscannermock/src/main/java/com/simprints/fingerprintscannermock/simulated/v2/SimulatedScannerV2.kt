package com.simprints.fingerprintscannermock.simulated.v2

import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager
import com.simprints.fingerprintscannermock.simulated.common.ScannerState
import com.simprints.fingerprintscannermock.simulated.common.SimulatedScanner
import java.io.OutputStream

class SimulatedScannerV2(simulatedScannerManager: SimulatedScannerManager,
                         scannerState: ScannerState = ScannerState())
    : SimulatedScanner(scannerState) {

    override fun handleAppToScannerEvent(bytes: ByteArray, returnStream: OutputStream) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
