package com.simprints.fingerprintscanner.component.bluetooth.android

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothDevice

class AndroidBluetoothAdapter(private val adapter: BluetoothAdapter?) : ComponentBluetoothAdapter {

    override fun isNull(): Boolean = adapter == null

    override fun isEnabled(): Boolean = adapter!!.isEnabled

    override fun getRemoteDevice(macAddress: String): ComponentBluetoothDevice =
            AndroidBluetoothDevice(adapter!!.getRemoteDevice(macAddress))

    /**
     * We suppress the permission check because it is not the scanner modules job to check the
     * Android permissions.
     */

    @SuppressLint("MissingPermission")
    override fun cancelDiscovery(): Boolean = adapter!!.cancelDiscovery()

    @SuppressLint("MissingPermission")
    override fun getBondedDevices(): Set<ComponentBluetoothDevice> =
            adapter!!.bondedDevices
                    .map { AndroidBluetoothDevice(it) }
                    .toSet()

    @SuppressLint("MissingPermission")
    override fun enable(): Boolean = adapter!!.enable()
}
