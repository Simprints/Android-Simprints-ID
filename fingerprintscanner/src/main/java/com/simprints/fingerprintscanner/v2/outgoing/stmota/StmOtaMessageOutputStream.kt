package com.simprints.fingerprintscanner.v2.outgoing.stmota

import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaCommand
import com.simprints.fingerprintscanner.v2.outgoing.OutgoingConnectable
import com.simprints.fingerprintscanner.v2.tools.reactive.completable
import io.reactivex.Completable
import java.io.OutputStream

class StmOtaMessageOutputStream : OutgoingConnectable {

    private lateinit var outputStream: OutputStream

    override fun connect(outputStream: OutputStream) {
        this.outputStream = outputStream
    }

    override fun disconnect() {
    }

    fun sendMessage(message: StmOtaCommand): Completable =
        dispatch(message.getBytes())

    private fun dispatch(packet: ByteArray): Completable = completable {
        outputStream.write(packet)
        outputStream.flush()
    }
}
