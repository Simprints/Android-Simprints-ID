package com.simprints.fingerprintscanner.v2.stream

import com.simprints.fingerprintscanner.v2.incoming.root.RootMessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.root.RootMessageOutputStream
import java.io.InputStream
import java.io.OutputStream

class RootMessageStream(val incoming: RootMessageInputStream,
                        val outgoing: RootMessageOutputStream) : Connectable {

    override fun connect(inputStream: InputStream, outputStream: OutputStream) {
        incoming.connect(inputStream)
        outgoing.connect(outputStream)
    }

    override fun disconnect() {
        outgoing.disconnect()
        incoming.disconnect()
    }
}
