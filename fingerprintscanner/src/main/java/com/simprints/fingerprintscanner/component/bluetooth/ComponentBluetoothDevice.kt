package com.simprints.fingerprintscanner.component.bluetooth

import java.io.IOException
import java.util.*


interface ComponentBluetoothDevice {

    val name: String?

    fun isBonded(): Boolean

    fun createBond(): Boolean

    fun removeBond(): Boolean

    @Throws(IOException::class)
    fun createRfcommSocketToServiceRecord(uuid: UUID): ComponentBluetoothSocket

    val address: String

    companion object {
        const val ACTION_BOND_STATE_CHANGED = "android.bluetooth.device.action.BOND_STATE_CHANGED"
        const val EXTRA_BOND_STATE = "android.bluetooth.device.extra.BOND_STATE"
        const val BOND_BONDED = 12
        const val BOND_NONE = 10
        const val EXTRA_REASON = "android.bluetooth.device.extra.REASON"
        const val BOND_SUCCESS = 0
        const val UNBOND_REASON_REMOVED = 9
    }
}
