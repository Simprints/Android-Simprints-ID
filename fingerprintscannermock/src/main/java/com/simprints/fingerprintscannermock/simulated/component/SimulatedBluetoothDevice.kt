package com.simprints.fingerprintscannermock.simulated.component

import com.simprints.fingerprintscanner.component.bluetooth.BluetoothComponentDevice
import com.simprints.fingerprintscanner.component.bluetooth.BluetoothComponentSocket
import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager
import java.util.*


class SimulatedBluetoothDevice(private val simulatedScannerManager: SimulatedScannerManager,
                               macAddress: String) : BluetoothComponentDevice {

    override var name: String = simulatedScannerManager.deviceName

    override fun isBonded(): Boolean = simulatedScannerManager.isDeviceBonded

    override fun createRfcommSocketToServiceRecord(uuid: UUID): BluetoothComponentSocket =
        SimulatedBluetoothSocket(simulatedScannerManager)

    override val address: String = macAddress
}
