package com.simprints.fingerprint.infra.scannermock.simulated.component

import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothSocket
import com.simprints.fingerprint.infra.scannermock.simulated.SimulatedScannerManager
import java.util.UUID


class SimulatedBluetoothDevice(private val simulatedScannerManager: SimulatedScannerManager,
                               macAddress: String) : ComponentBluetoothDevice {

    override var name: String? = simulatedScannerManager.deviceName

    override fun isBonded(): Boolean = simulatedScannerManager.isDeviceBonded

    override fun createBond(): Boolean {
        simulatedScannerManager.isDeviceBonded = true
        return true
    }

    override fun createRfcommSocketToServiceRecord(uuid: UUID): ComponentBluetoothSocket =
        SimulatedBluetoothSocket(simulatedScannerManager)

    override val address: String = macAddress
}
