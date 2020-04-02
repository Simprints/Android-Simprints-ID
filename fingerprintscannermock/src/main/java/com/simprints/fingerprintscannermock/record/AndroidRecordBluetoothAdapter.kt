package com.simprints.fingerprintscannermock.record

import android.bluetooth.BluetoothAdapter
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothDevice

class AndroidRecordBluetoothAdapter(private val adapter: BluetoothAdapter?,
                                    private val fileWithFakeBytes: String?): ComponentBluetoothAdapter {

    override fun isNull(): Boolean = adapter == null

    override fun isEnabled(): Boolean = adapter!!.isEnabled

    override fun getRemoteDevice(macAddress: String): ComponentBluetoothDevice =
            AndroidRecordBluetoothDevice(adapter!!.getRemoteDevice(macAddress), fileWithFakeBytes)

    override fun cancelDiscovery(): Boolean = adapter!!.cancelDiscovery()

    override fun getBondedDevices(): Set<ComponentBluetoothDevice> =
            adapter!!.bondedDevices
                    .map { AndroidRecordBluetoothDevice(it, fileWithFakeBytes) }
                    .toSet()

    override fun enable(): Boolean = adapter!!.enable()
}
