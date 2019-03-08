package com.simprints.libscanner.bluetooth.android

import android.bluetooth.BluetoothAdapter
import com.simprints.libscanner.bluetooth.BluetoothComponentAdapter
import com.simprints.libscanner.bluetooth.BluetoothComponentDevice

class AndroidBluetoothAdapter(private val adapter: BluetoothAdapter?) : BluetoothComponentAdapter {

    override fun isNull(): Boolean = adapter == null

    override fun isEnabled(): Boolean = adapter!!.isEnabled

    override fun getRemoteDevice(macAddress: String): BluetoothComponentDevice =
            AndroidBluetoothDevice(adapter!!.getRemoteDevice(macAddress))

    override fun cancelDiscovery(): Boolean = adapter!!.cancelDiscovery()

    override fun getBondedDevices(): Set<BluetoothComponentDevice> =
            adapter!!.bondedDevices
                    .map { AndroidBluetoothDevice(it) }
                    .toSet()
}
