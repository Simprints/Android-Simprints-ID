package com.simprints.fingerprintscanner.v2.message

import com.simprints.fingerprintscanner.v2.accumulator.accumulateAndTakeElements
import com.simprints.fingerprintscanner.v2.packets.Packet
import io.reactivex.Flowable

fun Flowable<out Packet>.toMessageStream(protocol: MessageProtocol, messageParser: MessageParser): Flowable<Message> = this
    .accumulateAndTakeElements(PacketToMessageAccumulator(protocol, messageParser))
