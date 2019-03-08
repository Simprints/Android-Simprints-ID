package com.simprints.mockscanner.record

import android.bluetooth.BluetoothAdapter
import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentAdapter
import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentDevice

class AndroidRecordBluetoothAdapter(private val adapter: BluetoothAdapter?,
                                    private val fileWithFakeBytes: String?): BluetoothComponentAdapter {

    override fun isNull(): Boolean = adapter == null

    override fun isEnabled(): Boolean = adapter!!.isEnabled

    override fun getRemoteDevice(macAddress: String): BluetoothComponentDevice =
            AndroidRecordBluetoothDevice(adapter!!.getRemoteDevice(macAddress), fileWithFakeBytes)

    override fun cancelDiscovery(): Boolean = adapter!!.cancelDiscovery()

    override fun getBondedDevices(): Set<BluetoothComponentDevice> =
            adapter!!.bondedDevices
                    .map { AndroidRecordBluetoothDevice(it, fileWithFakeBytes) }
                    .toSet()
}
