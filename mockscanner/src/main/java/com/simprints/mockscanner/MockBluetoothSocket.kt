package com.simprints.mockscanner

import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentSocket
import java.io.InputStream
import java.io.OutputStream


class MockBluetoothSocket(private val mockScannerManager: MockScannerManager) : BluetoothComponentSocket {

    override fun connect() = mockScannerManager.connect()

    override fun getInputStream(): InputStream = mockScannerManager.streamFromScannerToApp

    override fun getOutputStream(): OutputStream = mockScannerManager.streamFromAppToScanner

    override fun close() = mockScannerManager.close()
}
