package com.simprints.fingerprint.infra.scanner.component.bluetooth

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * An Interface representing operations to be run on a Bluetooth Socket. It represents operations
 * for connect, reading & writing to a Bluetooth Socket.
 */
interface ComponentBluetoothSocket {
    @Throws(IOException::class)
    fun connect()

    @Throws(IOException::class)
    fun getInputStream(): InputStream

    @Throws(IOException::class)
    fun getOutputStream(): OutputStream

    @Throws(IOException::class)
    fun close()
}
