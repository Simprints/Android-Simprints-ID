package com.simprints.fingerprintscannermock.simulated.v1

import com.simprints.fingerprintscanner.v1.Message
import com.simprints.fingerprintscannermock.simulated.common.ScannerState
import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager
import com.simprints.fingerprintscannermock.simulated.common.SimulatedScanner
import com.simprints.fingerprintscannermock.simulated.tools.bytesToMessageV1
import java.io.OutputStream

class SimulatedScannerV1(simulatedScannerManager: SimulatedScannerManager,
                         scannerState: ScannerState = ScannerState())
    : SimulatedScanner(scannerState) {

    private val responseHelper = SimulatedResponseHelperV1(simulatedScannerManager, this)

    override fun handleAppToScannerEvent(bytes: ByteArray, returnStream: OutputStream) {
        val message: Message = bytesToMessageV1(bytes)
        scannerState.updateStateAccordingToOutgoingMessage(message)
        val response: ByteArray = responseHelper.createMockResponse(message)
        writeResponseToStream(response, returnStream)
    }
}
