package com.simprints.fingerprintscanner.v2.incoming.message

import com.simprints.fingerprintscanner.v2.domain.message.Message
import com.simprints.fingerprintscanner.v2.domain.packet.Packet
import com.simprints.fingerprintscanner.v2.incoming.message.accumulators.PacketToMessageAccumulator
import com.simprints.fingerprintscanner.v2.tools.accumulator.accumulateAndTakeElements
import io.reactivex.Flowable

fun <R: Message> Flowable<out Packet>.toMessageStream(accumulator: PacketToMessageAccumulator<R>): Flowable<R> =
    accumulateAndTakeElements(accumulator)
