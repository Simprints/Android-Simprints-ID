package com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators

import com.simprints.fingerprintscanner.v2.domain.main.message.Message
import com.simprints.fingerprintscanner.v2.domain.main.message.MessageProtocol
import com.simprints.fingerprintscanner.v2.domain.main.packet.Packet
import com.simprints.fingerprintscanner.v2.incoming.main.message.parsers.MessageParser
import com.simprints.fingerprintscanner.v2.tools.accumulator.ByteArrayAccumulator

abstract class PacketToMessageAccumulator<R: Message>(
    private val protocol: MessageProtocol,
    private val messageParser: MessageParser<R>
) : ByteArrayAccumulator<Packet, R>(
    fragmentAsByteArray = { packet -> packet.payload },
    canComputeElementLength = { bytes -> bytes.size >= protocol.HEADER_SIZE },
    computeElementLength = { bytes -> protocol.getTotalLengthFromHeader(bytes.sliceArray(protocol.HEADER_INDICES)) },
    buildElement = { bytes -> messageParser.parse(bytes) }
)
