package com.simprints.fingerprintscanner.v2.outgoing.message

import com.simprints.fingerprintscanner.v2.domain.message.OutgoingMessage
import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20Command
import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.packet.Channel
import com.simprints.fingerprintscanner.v2.domain.packet.Packet
import com.simprints.fingerprintscanner.v2.domain.packet.PacketProtocol
import com.simprints.fingerprintscanner.v2.incoming.packet.PacketParser

class MessageSerializer(val packetParser: PacketParser) {

    fun serialize(message: OutgoingMessage): List<Packet> =
        message
            .getBytes()
            .asList()
            .chunked(PacketProtocol.MAX_PAYLOAD_SIZE)
            .map { it.toByteArray() }
            .map {
                val destination = when (message) {
                    is VeroCommand -> Channel.Remote.VeroServer
                    is Un20Command -> Channel.Remote.Un20Server
                    else -> TODO()
                }

                PacketProtocol.buildPacketBytes(Channel.Local.AndroidDevice, destination, it)
            }
            .map { packetParser.parse(it) }
}
