package com.simprints.mockscanner

import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentDevice
import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentSocket
import java.util.*


class MockBluetoothDevice(private val mockScannerManager: MockScannerManager,
                          macAddress: String) : BluetoothComponentDevice {

    override var name: String = mockScannerManager.deviceName

    override fun isBonded(): Boolean = mockScannerManager.isDeviceBonded

    override fun createRfcommSocketToServiceRecord(uuid: UUID): BluetoothComponentSocket =
            MockBluetoothSocket(mockScannerManager)

    override val address: String = macAddress
}
