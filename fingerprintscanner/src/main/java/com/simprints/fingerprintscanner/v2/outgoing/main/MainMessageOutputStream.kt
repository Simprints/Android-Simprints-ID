package com.simprints.fingerprintscanner.v2.outgoing.main

import com.simprints.fingerprintscanner.v2.domain.main.message.OutgoingMainMessage
import com.simprints.fingerprintscanner.v2.outgoing.OutgoingConnectable
import com.simprints.fingerprintscanner.v2.outgoing.main.message.MainMessageSerializer
import com.simprints.fingerprintscanner.v2.outgoing.main.packet.PacketDispatcher
import io.reactivex.Completable

class MainMessageOutputStream(
    private val mainMessageSerializer: MainMessageSerializer,
    private val packetDispatcher: PacketDispatcher
) : OutgoingConnectable by packetDispatcher {

    fun sendMessage(message: OutgoingMainMessage): Completable =
        mainMessageSerializer.serialize(message).let {
            packetDispatcher.dispatch(it)
        }
}
