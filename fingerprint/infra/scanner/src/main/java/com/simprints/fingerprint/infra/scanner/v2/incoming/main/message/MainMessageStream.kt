package com.simprints.fingerprint.infra.scanner.v2.incoming.main.message

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.MainMessage
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Packet
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.accumulators.PacketToMainMessageAccumulator
import com.simprints.fingerprint.infra.scanner.v2.tools.accumulator.accumulateAndTakeElements
import io.reactivex.Flowable

fun <R : MainMessage> Flowable<out Packet>.toMainMessageStream(accumulator: PacketToMainMessageAccumulator<R>): Flowable<R> =
    accumulateAndTakeElements(accumulator)
