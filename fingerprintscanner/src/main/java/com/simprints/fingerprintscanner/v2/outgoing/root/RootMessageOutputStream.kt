package com.simprints.fingerprintscanner.v2.outgoing.root

import com.simprints.fingerprintscanner.v2.domain.root.RootCommand
import com.simprints.fingerprintscanner.v2.outgoing.OutgoingConnectable
import com.simprints.fingerprintscanner.v2.tools.reactive.completable
import io.reactivex.Completable
import java.io.OutputStream

class RootMessageOutputStream(
    private val rootMessageSerializer: RootMessageSerializer
) : OutgoingConnectable {

    private lateinit var outputStream: OutputStream

    override fun connect(outputStream: OutputStream) {
        this.outputStream = outputStream
    }

    override fun disconnect() {
    }

    fun sendMessage(message: RootCommand): Completable =
        dispatch(rootMessageSerializer.serialize(message))

    private fun dispatch(packets: Iterable<ByteArray>): Completable =
        Completable.concat(packets.map { dispatch(it) })

    private fun dispatch(packet: ByteArray): Completable = completable {
        outputStream.write(packet)
        outputStream.flush()
    }
}
