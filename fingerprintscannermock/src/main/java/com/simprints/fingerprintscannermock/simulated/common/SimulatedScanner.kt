package com.simprints.fingerprintscannermock.simulated.common

import java.io.OutputStream

abstract class SimulatedScanner(var scannerState: ScannerState = ScannerState()) {

    abstract fun handleAppToScannerEvent(bytes: ByteArray, returnStream: OutputStream)

    protected fun writeResponseToStream(response: ByteArray, returnStream: OutputStream) {
        returnStream.write(response)
        returnStream.flush()
    }
}
