package com.simprints.mockscanner

import com.simprints.libscanner.bluetooth.BluetoothComponentAdapter
import com.simprints.libscanner.bluetooth.BluetoothComponentDevice


class MockBluetoothAdapter(private val mockScannerManager: MockScannerManager) : BluetoothComponentAdapter {

    override fun isNull(): Boolean = mockScannerManager.isAdapterNull

    override fun isEnabled(): Boolean = mockScannerManager.isAdapterEnabled

    override fun getRemoteDevice(macAddress: String): BluetoothComponentDevice = mockScannerManager.getScannerWithAddress(macAddress)

    override fun cancelDiscovery(): Boolean = true

    override fun getBondedDevices(): Set<BluetoothComponentDevice> = mockScannerManager.pairedScanners
}
