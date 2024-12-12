package com.simprints.fingerprint.infra.scanner.v2.incoming.main.packet

import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Packet
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.PacketProtocol.HEADER_INDICES
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.PacketProtocol.HEADER_SIZE
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.PacketProtocol.getTotalLengthFromHeader
import com.simprints.fingerprint.infra.scanner.v2.tools.accumulator.ByteArrayAccumulator
import javax.inject.Inject

class ByteArrayToPacketAccumulator @Inject constructor(
    private val packetParser: PacketParser,
) : ByteArrayAccumulator<ByteArray, Packet>(
        fragmentAsByteArray = { bytes -> bytes },
        canComputeElementLength = { bytes -> bytes.size >= HEADER_SIZE },
        computeElementLength = { bytes -> getTotalLengthFromHeader(bytes.sliceArray(HEADER_INDICES)) },
        buildElement = { bytes -> packetParser.parse(bytes) },
    )
