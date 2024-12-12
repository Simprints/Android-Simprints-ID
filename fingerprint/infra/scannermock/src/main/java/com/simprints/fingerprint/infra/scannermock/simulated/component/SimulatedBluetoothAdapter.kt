package com.simprints.fingerprint.infra.scannermock.simulated.component

import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.fingerprint.infra.scannermock.simulated.SimulatedScannerManager

class SimulatedBluetoothAdapter(
    private val simulatedScannerManager: SimulatedScannerManager,
) : ComponentBluetoothAdapter {
    override fun isNull(): Boolean = simulatedScannerManager.isAdapterNull

    override fun isEnabled(): Boolean = simulatedScannerManager.isAdapterEnabled

    override fun getRemoteDevice(macAddress: String): ComponentBluetoothDevice = simulatedScannerManager.getScannerWithAddress(macAddress)

    override fun cancelDiscovery(): Boolean = true

    override fun getBondedDevices(): Set<ComponentBluetoothDevice> = simulatedScannerManager.pairedScanners
}
