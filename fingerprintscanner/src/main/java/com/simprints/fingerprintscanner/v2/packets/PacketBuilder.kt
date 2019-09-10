package com.simprints.fingerprintscanner.v2.packets

class PacketBuilder {

    fun buildPacketFromBytes(bytes: ByteArray): Packet {

        val header = PacketProtocol.getHeaderBytes(bytes)

        return Packet(
            bytes = bytes,
            header = header,
            body = PacketProtocol.getBodyBytes(bytes),
            source = PacketProtocol.getSourceFromHeader(header),
            destination = PacketProtocol.getDestinationFromHeader(header),
            length = PacketProtocol.getPacketLengthFromHeader(header)
        )
    }

    fun buildPacket(source: Channel, destination: Channel, body: ByteArray): Packet =
        buildPacketFromBytes(PacketProtocol.buildPacketBytes(source, destination, body))
}
