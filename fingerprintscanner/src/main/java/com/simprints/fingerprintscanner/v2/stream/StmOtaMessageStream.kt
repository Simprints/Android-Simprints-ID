package com.simprints.fingerprintscanner.v2.stream

import com.simprints.fingerprintscanner.v2.incoming.stmota.StmOtaMessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.stmota.StmOtaMessageOutputStream
import java.io.InputStream
import java.io.OutputStream

class StmOtaMessageStream(val incoming: StmOtaMessageInputStream,
                          val outgoing: StmOtaMessageOutputStream) : Connectable {

    override fun connect(inputStream: InputStream, outputStream: OutputStream) {
        incoming.connect(inputStream)
        outgoing.connect(outputStream)
    }

    override fun disconnect() {
        outgoing.disconnect()
        incoming.disconnect()
    }
}
