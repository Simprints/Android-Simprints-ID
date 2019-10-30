package com.simprints.fingerprintscanner.v2.outgoing.packet

import com.simprints.fingerprintscanner.v2.domain.packet.Packet

class PacketSerializer {

    fun serialize(packet: Packet): ByteArray = packet.bytes
}
