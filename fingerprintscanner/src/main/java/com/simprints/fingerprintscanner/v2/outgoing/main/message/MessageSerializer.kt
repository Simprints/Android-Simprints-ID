package com.simprints.fingerprintscanner.v2.outgoing.main.message

import com.simprints.fingerprintscanner.v2.domain.main.message.OutgoingMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.main.packet.Channel
import com.simprints.fingerprintscanner.v2.domain.main.packet.Packet
import com.simprints.fingerprintscanner.v2.domain.main.packet.PacketProtocol
import com.simprints.fingerprintscanner.v2.incoming.main.packet.PacketParser

class MessageSerializer(private val packetParser: PacketParser) {

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
                    else -> TODO("exception handling")
                }

                PacketProtocol.buildPacketBytes(Channel.Local.AndroidDevice, destination, it)
            }
            .map { packetParser.parse(it) }
}
