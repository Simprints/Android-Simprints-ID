package com.simprints.fingerprintscanner.v2.incoming.packet

import com.simprints.fingerprintscanner.v2.domain.packet.Packet
import com.simprints.fingerprintscanner.v2.tools.accumulator.accumulateAndTakeElements
import io.reactivex.Flowable

fun Flowable<out ByteArray>.toPacketStream(accumulator: ByteArrayToPacketAccumulator): Flowable<Packet> = this
    .accumulateAndTakeElements(accumulator)
