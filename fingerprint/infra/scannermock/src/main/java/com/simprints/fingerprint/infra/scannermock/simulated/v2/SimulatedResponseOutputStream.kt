package com.simprints.fingerprint.infra.scannermock.simulated.v2

import com.simprints.fingerprint.infra.scanner.v2.domain.IncomingMessage
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.MainMessage
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroEvent
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.PacketProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Route
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessage
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.chunked

class SimulatedResponseOutputStream {
    fun serialize(message: IncomingMessage): List<ByteArray> = message
        .getBytes()
        .chunked(PacketProtocol.MAX_PAYLOAD_SIZE)
        .map {
            when (message) {
                is MainMessage -> {
                    val source = when (message) {
                        is VeroResponse -> Route.Remote.VeroServer
                        is VeroEvent -> Route.Remote.VeroEvent
                        is Un20Response -> Route.Remote.Un20Server
                        else -> throw IllegalArgumentException("Trying to serialize invalid simulated MainMessage")
                    }

                    PacketProtocol.buildPacketBytes(source, Route.Local.AndroidDevice, it)
                }
                is RootMessage -> {
                    it
                }
                else -> throw IllegalArgumentException("Trying to serialize invalid simulated Message")
            }
        }
}
