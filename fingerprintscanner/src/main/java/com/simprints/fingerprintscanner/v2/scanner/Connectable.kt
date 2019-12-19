package com.simprints.fingerprintscanner.v2.scanner

import java.io.InputStream
import java.io.OutputStream

interface Connectable {

    fun connect(inputStream: InputStream, outputStream: OutputStream)
    fun disconnect()
}
