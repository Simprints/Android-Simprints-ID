package com.simprints.fingerprint.infra.scanner.component.bluetooth

/**
 * Provides a one-to-one abstraction above [android.bluetooth.BluetoothAdapter] for use in mocking
 * or swapping for a component with different utility. Using this interface will give access to
 * [ComponentBluetoothDevice], which in turn can be used to create [ComponentBluetoothSocket] which
 * contains the actual [java.io.InputStream] and [java.io.OutputStream] for communication.
 *
 * Only functions that are required by the actual implementations have been added to these
 * interfaces.
 */
interface ComponentBluetoothAdapter {
    fun isNull(): Boolean

    fun isEnabled(): Boolean

    fun getRemoteDevice(macAddress: String): ComponentBluetoothDevice

    fun cancelDiscovery(): Boolean

    fun getBondedDevices(): Set<ComponentBluetoothDevice>

    companion object {
        const val ACTION_STATE_CHANGED = "android.bluetooth.adapter.action.STATE_CHANGED"
        const val EXTRA_STATE = "android.bluetooth.adapter.extra.STATE"
        const val ERROR = Int.MIN_VALUE
        const val STATE_ON = 12
    }
}
