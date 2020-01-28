package com.simprints.fingerprintscanner.v2.channel

import com.simprints.fingerprintscanner.v2.incoming.common.MessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.common.MessageOutputStream
import java.io.InputStream
import java.io.OutputStream

abstract class MessageChannel<I : MessageInputStream, O : MessageOutputStream<*>>(
    val incoming: I,
    val outgoing: O
) : Connectable {

    override fun connect(inputStream: InputStream, outputStream: OutputStream) {
        incoming.connect(inputStream)
        outgoing.connect(outputStream)
    }

    override fun disconnect() {
        outgoing.disconnect()
        incoming.disconnect()
    }
}
