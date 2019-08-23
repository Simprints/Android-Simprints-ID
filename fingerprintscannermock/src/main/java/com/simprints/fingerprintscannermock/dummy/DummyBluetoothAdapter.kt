package com.simprints.fingerprintscannermock.dummy

import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentAdapter
import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentDevice

class DummyBluetoothAdapter : BluetoothComponentAdapter {
    override fun isNull(): Boolean = false

    override fun isEnabled(): Boolean = true

    override fun getRemoteDevice(macAddress: String): BluetoothComponentDevice =
        throw UnsupportedOperationException("DummyBluetoothAdapter::getRemoteDevice")

    override fun cancelDiscovery(): Boolean = true

    override fun getBondedDevices(): Set<BluetoothComponentDevice> =
        throw UnsupportedOperationException("DummyBluetoothAdapter::getBondedDevices")
}
