package com.simprints.fingerprintscanner.v2.outgoing

import java.io.OutputStream

interface OutgoingConnectable {

    fun connect(outputStream: OutputStream)
    fun disconnect()
}
