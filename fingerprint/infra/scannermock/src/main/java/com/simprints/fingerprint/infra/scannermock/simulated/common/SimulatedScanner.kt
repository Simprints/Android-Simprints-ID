package com.simprints.fingerprint.infra.scannermock.simulated.common

import com.simprints.fingerprint.infra.scannermock.simulated.SimulatedScannerManager
import com.simprints.fingerprint.infra.scannermock.simulated.common.SimulationSpeedBehaviour.INSTANT
import com.simprints.fingerprint.infra.scannermock.simulated.common.SimulationSpeedBehaviour.REALISTIC
import java.io.OutputStream

abstract class SimulatedScanner(val scannerManager: SimulatedScannerManager) {

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

    abstract fun disconnect()
}
