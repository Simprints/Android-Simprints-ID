package com.simprints.fingerprintscannermock.simulated

import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentDevice
import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentSocket
import java.util.*


class SimulatedBluetoothDevice(private val simulatedScannerManager: SimulatedScannerManager,
                               macAddress: String) : BluetoothComponentDevice {

    override var name: String = simulatedScannerManager.deviceName

    override fun isBonded(): Boolean = simulatedScannerManager.isDeviceBonded

    override fun createRfcommSocketToServiceRecord(uuid: UUID): BluetoothComponentSocket =
        SimulatedBluetoothSocket(simulatedScannerManager)

    override val address: String = macAddress
}
