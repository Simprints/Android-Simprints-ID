package com.simprints.fingerprintscanner.v2.outgoing.main.message

import com.simprints.fingerprintscanner.v2.domain.main.message.OutgoingMainMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.main.packet.Channel
import com.simprints.fingerprintscanner.v2.domain.main.packet.Packet
import com.simprints.fingerprintscanner.v2.domain.main.packet.PacketProtocol
import com.simprints.fingerprintscanner.v2.incoming.main.packet.PacketParser
import com.simprints.fingerprintscanner.v2.tools.primitives.chunked

class MainMessageSerializer(private val packetParser: PacketParser) {

    fun serialize(message: OutgoingMainMessage): List<Packet> =
        message
            .getBytes()
            .chunked(PacketProtocol.MAX_PAYLOAD_SIZE)
            .map {
                val destination = when (message) {
                    is VeroCommand -> Channel.Remote.VeroServer
                    is Un20Command -> Channel.Remote.Un20Server
                    else -> TODO("exception handling")
                }

                PacketProtocol.buildPacketBytes(Channel.Local.AndroidDevice, destination, it)
            }
            .map { packetParser.parse(it) }
}
