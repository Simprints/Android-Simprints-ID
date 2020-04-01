package com.simprints.fingerprintscanner.component.bluetooth.android

import android.bluetooth.BluetoothAdapter
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothDevice

class AndroidBluetoothAdapter(private val adapter: BluetoothAdapter?) : ComponentBluetoothAdapter {

    override fun isNull(): Boolean = adapter == null

    override fun isEnabled(): Boolean = adapter!!.isEnabled

    override fun getRemoteDevice(macAddress: String): ComponentBluetoothDevice =
            AndroidBluetoothDevice(adapter!!.getRemoteDevice(macAddress))

    override fun cancelDiscovery(): Boolean = adapter!!.cancelDiscovery()

    override fun getBondedDevices(): Set<ComponentBluetoothDevice> =
            adapter!!.bondedDevices
                    .map { AndroidBluetoothDevice(it) }
                    .toSet()

    override fun enable(): Boolean = adapter!!.enable()
}
