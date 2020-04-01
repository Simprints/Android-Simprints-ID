package com.simprints.fingerprintscannermock.simulated.component

import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager


class SimulatedBluetoothAdapter(private val simulatedScannerManager: SimulatedScannerManager) : ComponentBluetoothAdapter {

    override fun isNull(): Boolean = simulatedScannerManager.isAdapterNull

    override fun isEnabled(): Boolean = simulatedScannerManager.isAdapterEnabled

    override fun getRemoteDevice(macAddress: String): ComponentBluetoothDevice = simulatedScannerManager.getScannerWithAddress(macAddress)

    override fun cancelDiscovery(): Boolean = true

    override fun getBondedDevices(): Set<ComponentBluetoothDevice> = simulatedScannerManager.pairedScanners

    override fun enable(): Boolean {
        simulatedScannerManager.isAdapterEnabled = true
        return true
    }
}
