package com.simprints.fingerprint.infra.scannermock.record

import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothDevice

/**
 * Android record bluetooth adapter could be used to debug scanner issues
 *
 * @property adapter
 * @constructor Create empty Android record bluetooth adapter
 */
@Suppress("unused")
class AndroidRecordBluetoothAdapter(
    private val adapter: BluetoothAdapter?,
    private val context: Context,
) : ComponentBluetoothAdapter {
    override fun isNull(): Boolean = adapter == null

    override fun isEnabled(): Boolean = adapter!!.isEnabled

    override fun getRemoteDevice(macAddress: String): ComponentBluetoothDevice =
        AndroidRecordBluetoothDevice(adapter!!.getRemoteDevice(macAddress), context)

    override fun cancelDiscovery(): Boolean = adapter!!.cancelDiscovery()

    override fun getBondedDevices(): Set<ComponentBluetoothDevice> = adapter!!
        .bondedDevices
        .map { AndroidRecordBluetoothDevice(it, context) }
        .toSet()
}
