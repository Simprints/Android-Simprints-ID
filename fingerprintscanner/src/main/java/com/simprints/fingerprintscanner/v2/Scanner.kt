package com.simprints.fingerprintscanner.v2

import android.bluetooth.BluetoothSocket
import java.io.Closeable

class Scanner(
    private val socket: BluetoothSocket
) : Closeable by socket {

    private val outputStream = socket.outputStream
    private val inputStream = socket.inputStream

}
