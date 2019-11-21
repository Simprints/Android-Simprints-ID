package com.simprints.fingerprintscannermock.simulated.common

import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager
import com.simprints.fingerprintscannermock.simulated.common.SimulationSpeedBehaviour.INSTANT
import com.simprints.fingerprintscannermock.simulated.common.SimulationSpeedBehaviour.REALISTIC
import java.io.OutputStream

abstract class SimulatedScanner(val scannerManager: SimulatedScannerManager,
                                var scannerState: ScannerState = ScannerState()) {

    abstract fun handleAppToScannerEvent(bytes: ByteArray, returnStream: OutputStream)

    protected fun writeResponseToStream(response: ByteArray, returnStream: OutputStream) {
        when (scannerManager.simulationSpeedBehaviour) {
            INSTANT -> writeResponseInstantly(response, returnStream)
            REALISTIC -> writeResponseChunkedWithTimeBetweenPackets(response, returnStream)
        }

    }

    private fun writeResponseInstantly(response: ByteArray, returnStream: OutputStream) {
        returnStream.write(response)
        returnStream.flush()
    }

    private fun writeResponseChunkedWithTimeBetweenPackets(response: ByteArray, returnStream: OutputStream) {
        response
            .toList()
            .chunked(RealisticSpeedBehaviour.PACKET_CHUNK_SIZE_BYTES)
            .map { it.toByteArray() }
            .forEach { packet ->
                Thread.sleep(RealisticSpeedBehaviour.DELAY_BETWEEN_PACKETS_MS)
                returnStream.write(packet)
                returnStream.flush()
            }
    }
}
