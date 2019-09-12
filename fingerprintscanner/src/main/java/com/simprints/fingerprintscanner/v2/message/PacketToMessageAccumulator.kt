package com.simprints.fingerprintscanner.v2.message

import com.simprints.fingerprintscanner.v2.accumulator.ByteArrayAccumulator
import com.simprints.fingerprintscanner.v2.packets.Packet

class PacketToMessageAccumulator(
    private val protocol: MessageProtocol,
    private val messageParser: MessageParser
) : ByteArrayAccumulator<Packet, Message>(
    fragmentAsByteArray = { packet -> packet.payload },
    canComputeElementLength = { bytes -> bytes.size >= protocol.HEADER_SIZE },
    computeElementLength = { bytes -> protocol.getTotalLengthFromHeader(bytes.sliceArray(protocol.HEADER_INDICES)) },
    buildElement = { bytes -> messageParser.parse(bytes) }
)
