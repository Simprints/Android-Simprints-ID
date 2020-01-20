package com.simprints.fingerprintscannermock.simulated.v2

import com.simprints.fingerprintscanner.v2.domain.main.message.IncomingMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroEvent
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.main.packet.Channel
import com.simprints.fingerprintscanner.v2.domain.main.packet.PacketProtocol

class SimulatedResponseOutputStream {

    fun serialize(message: IncomingMessage): List<ByteArray> =
        message
            .getBytes()
            .asList()
            .chunked(PacketProtocol.MAX_PAYLOAD_SIZE)
            .map { it.toByteArray() }
            .map {
                val source = when (message) {
                    is VeroResponse -> Channel.Remote.VeroServer
                    is VeroEvent -> Channel.Remote.VeroEvent
                    is Un20Response -> Channel.Remote.Un20Server
                    else -> throw IllegalArgumentException("Trying to serialize invalid simulated message")
                }

                PacketProtocol.buildPacketBytes(source, Channel.Local.AndroidDevice, it)
            }

}
