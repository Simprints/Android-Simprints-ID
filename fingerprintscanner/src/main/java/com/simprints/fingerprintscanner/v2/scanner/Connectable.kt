package com.simprints.fingerprintscanner.v2.scanner

import com.simprints.fingerprintscanner.api.bluetooth.BluetoothComponentSocket

interface Connectable {

    fun connect(socket: BluetoothComponentSocket)
    fun disconnect()
}
