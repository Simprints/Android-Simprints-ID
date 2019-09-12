package com.simprints.fingerprintscanner.v2.incoming.packet

import com.simprints.fingerprintscanner.v2.domain.packet.Packet
import com.simprints.fingerprintscanner.v2.domain.packet.PacketProtocol

class PacketParser {

    fun parse(bytes: ByteArray): Packet =
        PacketProtocol.getHeaderBytes(bytes).let { header ->
            Packet(
                bytes = bytes,
                header = header,
                payload = PacketProtocol.getPayloadBytes(bytes),
                source = PacketProtocol.getSourceFromHeader(header),
                destination = PacketProtocol.getDestinationFromHeader(header),
                payloadLength = PacketProtocol.getPayloadLengthFromHeader(header)
            )
        }
}
