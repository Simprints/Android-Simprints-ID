package com.simprints.fingerprint.infra.scannermock.record

import android.bluetooth.BluetoothSocket
import android.content.Context
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothSocket
import org.apache.commons.io.input.TeeInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class AndroidRecordBluetoothSocket(
    private val socket: BluetoothSocket,
    private val context: Context,
) : ComponentBluetoothSocket {
    override fun connect() = socket.connect()

    override fun getInputStream(): InputStream = // we forward all bytes through the FileOutputStream before consuming them
        TeeInputStream(
            socket.inputStream,
            File("${context.filesDir}/${DEBUG_FILE_NAME}").let {
                FileOutputStream("${context.filesDir}/${DEBUG_FILE_NAME}")
            },
        )

    override fun getOutputStream(): OutputStream = socket.outputStream

    override fun close() = socket.close()

    companion object {
        const val DEBUG_FILE_NAME = "recorded-scanner-bytes"
    }
}
