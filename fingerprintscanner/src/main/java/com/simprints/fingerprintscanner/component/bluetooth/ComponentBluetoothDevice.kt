package com.simprints.fingerprintscanner.component.bluetooth

import java.io.IOException
import java.util.*


interface ComponentBluetoothDevice {

    val name: String

    fun isBonded(): Boolean

    @Throws(IOException::class)
    fun createRfcommSocketToServiceRecord(uuid: UUID): ComponentBluetoothSocket

    val address: String
}
