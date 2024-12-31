package com.simprints.fingerprint.infra.scanner.v2.incoming.main.packet

import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Packet
import com.simprints.fingerprint.infra.scanner.v2.tools.accumulator.accumulateAndTakeElements
import kotlinx.coroutines.flow.Flow

fun Flow<ByteArray>.toPacketStream(accumulator: ByteArrayToPacketAccumulator): Flow<Packet> = accumulateAndTakeElements(accumulator)
