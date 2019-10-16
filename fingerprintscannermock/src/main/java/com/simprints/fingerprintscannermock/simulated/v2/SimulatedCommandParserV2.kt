package com.simprints.fingerprintscannermock.simulated.v2

import com.simprints.fingerprintscanner.v2.incoming.message.accumulators.PacketToMessageAccumulator
import com.simprints.fingerprintscanner.v2.incoming.message.toMessageStream
import com.simprints.fingerprintscanner.v2.incoming.packet.ByteArrayToPacketAccumulator
import com.simprints.fingerprintscanner.v2.incoming.packet.PacketParser
import com.simprints.fingerprintscanner.v2.incoming.packet.toPacketStream
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.PublishSubject

class SimulatedCommandParserV2() {

    private val inputStreamObservableEmitter = PublishSubject.create<ByteArray>()
    val inputStreamFlowable = inputStreamObservableEmitter.toFlowable(BackpressureStrategy.BUFFER)
    val messageStreamFlowable = inputStreamFlowable
        .toPacketStream(ByteArrayToPacketAccumulator(PacketParser()))
        .toMessageStream()

    fun updateWithNewBytes(bytes: ByteArray) {
        inputStreamObservableEmitter.onNext(bytes)
    }

}

class FirmwarePacketToMessageAccumulator : PacketToMessageAccumulator<>
