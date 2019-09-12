package com.simprints.fingerprintscanner.v2.outgoing.packet

import com.simprints.fingerprintscanner.v2.domain.packet.Packet
import com.simprints.fingerprintscanner.v2.outgoing.OutgoingConnectable
import com.simprints.fingerprintscanner.v2.tools.completable
import io.reactivex.Completable
import java.io.OutputStream

class PacketDispatcher(private val packetSerializer: PacketSerializer) : OutgoingConnectable {

    private lateinit var outputStream: OutputStream

    override fun connect(outputStream: OutputStream) {
        this.outputStream = outputStream
    }

    override fun disconnect() {
    }

    fun dispatch(packets: Iterable<Packet>): Completable =
        Completable.concat(packets.map { dispatch(it) })

    private fun dispatch(packet: Packet): Completable = completable {
        val bytes = packetSerializer.serialize(packet)
        outputStream.write(bytes)
        outputStream.flush()
    }
}
