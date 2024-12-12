package com.simprints.fingerprint.infra.scanner.component.bluetooth.android

import android.bluetooth.BluetoothSocket
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothSocket
import java.io.InputStream
import java.io.OutputStream

internal class AndroidBluetoothSocket(
    private val socket: BluetoothSocket,
) : ComponentBluetoothSocket {
    override fun connect() = socket.connect()

    override fun getInputStream(): InputStream = socket.inputStream

    override fun getOutputStream(): OutputStream = socket.outputStream

    override fun close() = socket.close()
}
