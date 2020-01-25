package com.simprints.fingerprintscanner.v2.outgoing.main

import com.simprints.fingerprintscanner.v2.domain.main.message.OutgoingMainMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.main.packet.Channel
import com.simprints.fingerprintscanner.v2.domain.main.packet.PacketProtocol
import com.simprints.fingerprintscanner.v2.outgoing.MessageSerializer
import com.simprints.fingerprintscanner.v2.tools.primitives.chunked

class MainMessageSerializer : MessageSerializer<OutgoingMainMessage> {

    override fun serialize(message: OutgoingMainMessage): List<ByteArray> =
        message
            .getBytes()
            .chunked(PacketProtocol.MAX_PAYLOAD_SIZE)
            .map {
                val destination = when (message) {
                    is VeroCommand -> Channel.Remote.VeroServer
                    is Un20Command -> Channel.Remote.Un20Server
                    else -> throw IllegalArgumentException("Trying to serialize invalid message type ")
                }

                PacketProtocol.buildPacketBytes(Channel.Local.AndroidDevice, destination, it)
            }
}
