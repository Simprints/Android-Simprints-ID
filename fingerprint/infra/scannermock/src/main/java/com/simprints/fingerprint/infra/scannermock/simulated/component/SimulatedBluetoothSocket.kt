package com.simprints.fingerprint.infra.scannermock.simulated.component

import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothSocket
import com.simprints.fingerprint.infra.scannermock.simulated.SimulatedScannerManager
import java.io.InputStream
import java.io.OutputStream


class SimulatedBluetoothSocket(private val simulatedScannerManager: SimulatedScannerManager) : ComponentBluetoothSocket {

    override fun connect() = simulatedScannerManager.connect()

    override fun getInputStream(): InputStream = simulatedScannerManager.streamFromScannerToApp

    override fun getOutputStream(): OutputStream = simulatedScannerManager.streamFromAppToScanner

    override fun close() = simulatedScannerManager.close()
}
