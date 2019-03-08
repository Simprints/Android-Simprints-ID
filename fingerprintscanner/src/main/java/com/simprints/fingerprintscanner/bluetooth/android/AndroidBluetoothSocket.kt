package com.simprints.fingerprintscanner.bluetooth.android

import android.bluetooth.BluetoothSocket
import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentSocket
import java.io.InputStream
import java.io.OutputStream


class AndroidBluetoothSocket(private val socket: BluetoothSocket): BluetoothComponentSocket {

    override fun connect() = socket.connect()

    override fun getInputStream(): InputStream = socket.inputStream

    override fun getOutputStream(): OutputStream = socket.outputStream

    override fun close() = socket.close()
}
