package com.simprints.fingerprintscanner.api.bluetooth


interface BluetoothComponentAdapter {

    fun isNull(): Boolean

    fun isEnabled(): Boolean

    fun getRemoteDevice(macAddress: String): BluetoothComponentDevice

    fun cancelDiscovery(): Boolean

    fun getBondedDevices(): Set<BluetoothComponentDevice>
}
