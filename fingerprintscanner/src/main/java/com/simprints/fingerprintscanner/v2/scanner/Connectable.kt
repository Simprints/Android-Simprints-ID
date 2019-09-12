package com.simprints.fingerprintscanner.v2.scanner

import android.bluetooth.BluetoothSocket

interface Connectable {

    fun connect(socket: BluetoothSocket)
    fun disconnect()
}
