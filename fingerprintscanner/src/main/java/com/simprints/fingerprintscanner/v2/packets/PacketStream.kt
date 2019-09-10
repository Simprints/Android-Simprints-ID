package com.simprints.fingerprintscanner.v2.packets

import com.simprints.fingerprintscanner.v2.accumulator.accumulateAndTakeElements
import com.simprints.fingerprintscanner.v2.tools.toFlowable
import io.reactivex.Flowable
import java.io.InputStream

fun InputStream.toPacketStream(): Flowable<Packet> = this
    .toFlowable()
    .toPacketStream()

fun Flowable<out ByteArray>.toPacketStream(): Flowable<Packet> = this
    .accumulateAndTakeElements(ByteArrayToPacketAccumulator())
