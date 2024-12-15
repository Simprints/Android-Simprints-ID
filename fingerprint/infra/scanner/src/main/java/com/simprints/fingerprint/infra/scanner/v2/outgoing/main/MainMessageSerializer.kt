package com.simprints.fingerprint.infra.scanner.v2.outgoing.main

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.OutgoingMainMessage
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.PacketProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Route
import com.simprints.fingerprint.infra.scanner.v2.outgoing.common.MessageSerializer
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.chunked
import javax.inject.Inject

class MainMessageSerializer @Inject constructor() : MessageSerializer<OutgoingMainMessage> {
    override fun serialize(message: OutgoingMainMessage): List<ByteArray> = message
        .getBytes()
        .chunked(PacketProtocol.MAX_PAYLOAD_SIZE)
        .map {
            val destination = when (message) {
                is VeroCommand -> Route.Remote.VeroServer
                is Un20Command -> Route.Remote.Un20Server
                else -> throw IllegalArgumentException("Trying to serialize invalid message type ")
            }

            PacketProtocol.buildPacketBytes(Route.Local.AndroidDevice, destination, it)
        }
}
