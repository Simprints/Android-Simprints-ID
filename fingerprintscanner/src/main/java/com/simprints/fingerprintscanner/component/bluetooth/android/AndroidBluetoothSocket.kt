package com.simprints.fingerprintscanner.component.bluetooth.android

import android.bluetooth.BluetoothSocket
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothSocket
import java.io.InputStream
import java.io.OutputStream


class AndroidBluetoothSocket(private val socket: BluetoothSocket): ComponentBluetoothSocket {

    override fun connect() = socket.connect()

    override fun getInputStream(): InputStream = socket.inputStream

    override fun getOutputStream(): OutputStream = socket.outputStream

    override fun close() = socket.close()
}
