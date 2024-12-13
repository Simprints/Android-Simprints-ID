package com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.accumulators

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.MainMessage
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.MessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Packet
import com.simprints.fingerprint.infra.scanner.v2.incoming.common.MessageParser
import com.simprints.fingerprint.infra.scanner.v2.tools.accumulator.ByteArrayAccumulator

abstract class PacketToMainMessageAccumulator<R : MainMessage>(
    private val protocol: MessageProtocol,
    private val messageParser: MessageParser<R>,
) : ByteArrayAccumulator<Packet, R>(
        fragmentAsByteArray = { packet -> packet.payload },
        canComputeElementLength = { bytes -> bytes.size >= protocol.headerSize },
        computeElementLength = { bytes -> protocol.getTotalLengthFromHeader(bytes.sliceArray(protocol.headerIndices)) },
        buildElement = { bytes -> messageParser.parse(bytes) },
    )
