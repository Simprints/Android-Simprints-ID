package com.simprints.fingerprintscanner.v2.scanner

import com.simprints.fingerprintscanner.component.bluetooth.BluetoothComponentSocket

interface Connectable {

    fun connect(socket: BluetoothComponentSocket)
    fun disconnect()
}
