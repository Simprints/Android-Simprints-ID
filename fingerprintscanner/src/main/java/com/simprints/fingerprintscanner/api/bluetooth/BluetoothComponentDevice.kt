package com.simprints.fingerprintscanner.api.bluetooth

import java.io.IOException
import java.util.*


interface BluetoothComponentDevice {

    val name: String

    fun isBonded(): Boolean

    @Throws(IOException::class)
    fun createRfcommSocketToServiceRecord(uuid: UUID): BluetoothComponentSocket

    val address: String
}
