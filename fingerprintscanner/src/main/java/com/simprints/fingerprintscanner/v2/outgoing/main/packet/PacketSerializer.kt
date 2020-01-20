package com.simprints.fingerprintscanner.v2.outgoing.main.packet

import com.simprints.fingerprintscanner.v2.domain.main.packet.Packet

class PacketSerializer {

    fun serialize(packet: Packet): ByteArray = packet.bytes
}
