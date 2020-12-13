package com.simprints.fingerprintscanner.v2.channel

import com.simprints.fingerprintscanner.v2.incoming.IncomingConnectable
import com.simprints.fingerprintscanner.v2.outgoing.OutgoingConnectable
import java.io.InputStream
import java.io.OutputStream

/**
 * Interface for a class that depends on both [IncomingConnectable]s and [OutgoingConnectable]s
 */
interface Connectable {

    fun connect(inputStream: InputStream, outputStream: OutputStream)
    fun disconnect()
}
