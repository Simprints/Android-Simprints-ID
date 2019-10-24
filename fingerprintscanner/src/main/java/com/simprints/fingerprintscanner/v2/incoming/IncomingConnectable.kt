package com.simprints.fingerprintscanner.v2.incoming

import java.io.InputStream

interface IncomingConnectable {

    fun connect(inputStream: InputStream)
    fun disconnect()
}
