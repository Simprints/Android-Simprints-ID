package com.simprints.fingerprintscannermock.simulated.v1

import com.simprints.fingerprintscanner.v1.Message
import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager
import com.simprints.fingerprintscannermock.simulated.common.SimulatedScanner
import java.io.OutputStream

class SimulatedScannerV1(simulatedScannerManager: SimulatedScannerManager,
                         val scannerState: SimulatedScannerStateV1 = SimulatedScannerStateV1())
    : SimulatedScanner(simulatedScannerManager) {

    private val responseHelper = SimulatedResponseHelperV1(simulatedScannerManager, this)

    override fun handleAppToScannerEvent(bytes: ByteArray, returnStream: OutputStream) {
        val message: Message = bytes.toMessageV1()
        scannerState.updateStateAccordingToOutgoingMessage(message)
        val response: ByteArray = responseHelper.createMockResponse(message)
        writeResponseToStream(response, returnStream)
    }
}
