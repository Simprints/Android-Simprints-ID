package com.simprints.fingerprintscanner.v2.incoming.packet

import com.simprints.fingerprintscanner.v2.domain.packet.Packet
import com.simprints.fingerprintscanner.v2.tools.accumulator.ByteArrayAccumulator
import com.simprints.fingerprintscanner.v2.domain.packet.PacketProtocol.HEADER_INDICES
import com.simprints.fingerprintscanner.v2.domain.packet.PacketProtocol.HEADER_SIZE
import com.simprints.fingerprintscanner.v2.domain.packet.PacketProtocol.getTotalLengthFromHeader

class ByteArrayToPacketAccumulator(
    private val packetParser: PacketParser
) : ByteArrayAccumulator<ByteArray, Packet>(
    fragmentAsByteArray = { bytes -> bytes },
    canComputeElementLength = { bytes -> bytes.size >= HEADER_SIZE },
    computeElementLength = { bytes -> getTotalLengthFromHeader(bytes.sliceArray(HEADER_INDICES)) },
    buildElement = { bytes -> packetParser.parse(bytes) }
)
