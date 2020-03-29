package com.simprints.fingerprintscannermock.simulated.component

import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothSocket
import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager
import java.util.*


class SimulatedBluetoothDevice(private val simulatedScannerManager: SimulatedScannerManager,
                               macAddress: String) : ComponentBluetoothDevice {

    override var name: String = simulatedScannerManager.deviceName

    override fun isBonded(): Boolean = simulatedScannerManager.isDeviceBonded

    override fun createBond(): Boolean {
        simulatedScannerManager.isDeviceBonded = true
        return true
    }

    override fun removeBond(): Boolean {
        simulatedScannerManager.isDeviceBonded = false
        return true
    }

    override fun createRfcommSocketToServiceRecord(uuid: UUID): ComponentBluetoothSocket =
        SimulatedBluetoothSocket(simulatedScannerManager)

    override val address: String = macAddress
}
