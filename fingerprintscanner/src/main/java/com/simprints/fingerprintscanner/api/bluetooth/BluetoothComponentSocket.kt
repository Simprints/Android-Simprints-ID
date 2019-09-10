package com.simprints.fingerprintscanner.api.bluetooth

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


interface BluetoothComponentSocket {

    @Throws(IOException::class)
    fun connect()

    @Throws(IOException::class)
    fun getInputStream(): InputStream

    @Throws(IOException::class)
    fun getOutputStream(): OutputStream

    @Throws(IOException::class)
    fun close()
}
