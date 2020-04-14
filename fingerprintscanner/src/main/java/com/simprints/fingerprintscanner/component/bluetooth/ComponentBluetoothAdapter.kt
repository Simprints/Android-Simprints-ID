package com.simprints.fingerprintscanner.component.bluetooth


interface ComponentBluetoothAdapter {

    fun isNull(): Boolean

    fun isEnabled(): Boolean

    fun getRemoteDevice(macAddress: String): ComponentBluetoothDevice

    fun cancelDiscovery(): Boolean

    fun getBondedDevices(): Set<ComponentBluetoothDevice>

    fun enable(): Boolean

    companion object {
        const val ACTION_STATE_CHANGED = "android.bluetooth.adapter.action.STATE_CHANGED"
        const val EXTRA_STATE = "android.bluetooth.adapter.extra.STATE"
        const val ERROR = Int.MIN_VALUE
        const val STATE_ON = 12
    }
}
