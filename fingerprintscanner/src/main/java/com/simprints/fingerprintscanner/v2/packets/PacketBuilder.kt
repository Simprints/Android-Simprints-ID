package com.simprints.fingerprintscanner.v2.packets

class PacketBuilder {

    fun build(bytes: ByteArray): Packet {

        val header = PacketProtocol.getHeaderBytes(bytes)

        return Packet(
            bytes = bytes,
            header = header,
            payload = PacketProtocol.getPayloadBytes(bytes),
            source = PacketProtocol.getSourceFromHeader(header),
            destination = PacketProtocol.getDestinationFromHeader(header),
            payloadLength = PacketProtocol.getPayloadLengthFromHeader(header)
        )
    }

    fun buildPacket(source: Channel, destination: Channel, body: ByteArray): Packet =
        build(PacketProtocol.buildPacketBytes(source, destination, body))
}
