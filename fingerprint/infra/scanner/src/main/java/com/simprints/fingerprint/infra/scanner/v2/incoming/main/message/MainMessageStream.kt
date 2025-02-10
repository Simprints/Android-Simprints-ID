package com.simprints.fingerprint.infra.scanner.v2.incoming.main.message

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.MainMessage
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Packet
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.accumulators.PacketToMainMessageAccumulator
import com.simprints.fingerprint.infra.scanner.v2.tools.accumulator.accumulateAndTakeElements
import kotlinx.coroutines.flow.Flow

fun <R : MainMessage> Flow<Packet>.toMainMessageStream(accumulator: PacketToMainMessageAccumulator<R>): Flow<R> =
    accumulateAndTakeElements(accumulator)
