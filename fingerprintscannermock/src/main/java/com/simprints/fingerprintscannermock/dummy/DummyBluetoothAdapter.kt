package com.simprints.fingerprintscannermock.dummy

import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothDevice

class DummyBluetoothAdapter : ComponentBluetoothAdapter {
    override fun isNull(): Boolean = false

    override fun isEnabled(): Boolean = true

    override fun getRemoteDevice(macAddress: String): ComponentBluetoothDevice =
        throw UnsupportedOperationException("DummyBluetoothAdapter::getRemoteDevice")

    override fun cancelDiscovery(): Boolean = true

    override fun getBondedDevices(): Set<ComponentBluetoothDevice> =
        throw UnsupportedOperationException("DummyBluetoothAdapter::getBondedDevices")

    override fun enable(): Boolean = true
}
