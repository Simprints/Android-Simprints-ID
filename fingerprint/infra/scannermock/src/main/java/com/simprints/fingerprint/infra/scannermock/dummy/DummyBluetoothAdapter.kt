package com.simprints.fingerprint.infra.scannermock.dummy

import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothDevice

class DummyBluetoothAdapter : ComponentBluetoothAdapter {
    override fun isNull(): Boolean = false

    override fun isEnabled(): Boolean = true

    override fun getRemoteDevice(macAddress: String): ComponentBluetoothDevice =
        throw UnsupportedOperationException("DummyBluetoothAdapter::getRemoteDevice")

    override fun cancelDiscovery(): Boolean = true

    override fun getBondedDevices(): Set<ComponentBluetoothDevice> =
        throw UnsupportedOperationException("DummyBluetoothAdapter::getBondedDevices")
}
