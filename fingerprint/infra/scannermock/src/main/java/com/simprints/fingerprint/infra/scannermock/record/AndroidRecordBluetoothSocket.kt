package com.simprints.fingerprint.infra.scannermock.record

import android.bluetooth.BluetoothSocket
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothSocket
import org.apache.commons.io.input.TeeInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class AndroidRecordBluetoothSocket(
    private val socket: BluetoothSocket,
    private val fileWithFakeBytes: String?,
) : ComponentBluetoothSocket {
    override fun connect() = socket.connect()

    override fun getInputStream(): InputStream = fileWithFakeBytes?.let {
        // we forward all bytes through the FileOutputStream before consuming them
        TeeInputStream(
            socket.inputStream,
            File(fileWithFakeBytes).delete().let {
                FileOutputStream(fileWithFakeBytes)
            },
        )
    } ?: socket.inputStream

    override fun getOutputStream(): OutputStream = socket.outputStream

    override fun close() = socket.close()
}
