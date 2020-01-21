package com.simprints.fingerprintscanner.v2.stream

import com.simprints.fingerprintscanner.v2.incoming.main.MainMessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.main.MainMessageOutputStream
import java.io.InputStream
import java.io.OutputStream

class MainMessageStream(val incoming: MainMessageInputStream,
                        val outgoing: MainMessageOutputStream) : Connectable {

    override fun connect(inputStream: InputStream, outputStream: OutputStream) {
        incoming.connect(inputStream)
        outgoing.connect(outputStream)
    }

    override fun disconnect() {
        outgoing.disconnect()
        incoming.disconnect()
    }
}
