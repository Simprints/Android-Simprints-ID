package com.simprints.fingerprintscannermock.simulated

import com.simprints.fingerprintscanner.api.bluetooth.BluetoothComponentSocket
import java.io.InputStream
import java.io.OutputStream


class SimulatedBluetoothSocket(private val simulatedScannerManager: SimulatedScannerManager) : BluetoothComponentSocket {

    override fun connect() = simulatedScannerManager.connect()

    override fun getInputStream(): InputStream = simulatedScannerManager.streamFromScannerToApp

    override fun getOutputStream(): OutputStream = simulatedScannerManager.streamFromAppToScanner

    override fun close() = simulatedScannerManager.close()
}
