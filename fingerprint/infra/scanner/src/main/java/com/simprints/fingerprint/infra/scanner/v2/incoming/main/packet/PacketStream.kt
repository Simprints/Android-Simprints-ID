package com.simprints.fingerprint.infra.scanner.v2.incoming.main.packet

import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Packet
import com.simprints.fingerprint.infra.scanner.v2.tools.accumulator.accumulateAndTakeElements
import io.reactivex.Flowable

fun Flowable<out ByteArray>.toPacketStream(accumulator: ByteArrayToPacketAccumulator): Flowable<Packet> = this
    .accumulateAndTakeElements(accumulator)
