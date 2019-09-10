package com.simprints.fingerprintscanner.v2.message

import com.simprints.fingerprintscanner.v2.accumulator.ByteArrayAccumulator
import com.simprints.fingerprintscanner.v2.packets.Packet

class PacketToMessageAccumulator(
    private val protocol: MessageProtocol,
    private val messageParser: MessageParser = MessageParser()
) : ByteArrayAccumulator<Packet, Message>(
    fragmentAsByteArray = { packet -> packet.body },
    canComputeElementLength = { bytes -> bytes.size >= protocol.HEADER_SIZE },
    computeElementLength = { bytes -> protocol.getMessageLengthFromHeader(bytes.sliceArray(protocol.HEADER_INDICES)) },
    buildElement = { bytes -> messageParser.parse(bytes) }
)
