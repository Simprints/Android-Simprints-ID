package com.simprints.fingerprintscanner.v2.packets

import com.simprints.fingerprintscanner.v2.accumulator.ByteArrayAccumulator
import com.simprints.fingerprintscanner.v2.packets.PacketProtocol.HEADER_INDICES
import com.simprints.fingerprintscanner.v2.packets.PacketProtocol.HEADER_SIZE
import com.simprints.fingerprintscanner.v2.packets.PacketProtocol.getPacketLengthFromHeader

class ByteArrayToPacketAccumulator(
    private val packetBuilder: PacketBuilder = PacketBuilder()
) : ByteArrayAccumulator<ByteArray, Packet>(
    fragmentAsByteArray = { bytes -> bytes },
    canComputeElementLength = { bytes -> bytes.size >= HEADER_SIZE },
    computeElementLength = { bytes -> getPacketLengthFromHeader(bytes.sliceArray(HEADER_INDICES)) },
    buildElement = { bytes -> packetBuilder.buildPacketFromBytes(bytes) }
)
