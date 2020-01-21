package com.simprints.fingerprintscanner.v2.outgoing.main

import com.simprints.fingerprintscanner.v2.domain.main.message.OutgoingMainMessage
import com.simprints.fingerprintscanner.v2.outgoing.OutgoingConnectable
import com.simprints.fingerprintscanner.v2.outgoing.main.message.MessageSerializer
import com.simprints.fingerprintscanner.v2.outgoing.main.packet.PacketDispatcher
import io.reactivex.Completable

class MessageOutputStream(
    private val messageSerializer: MessageSerializer,
    private val packetDispatcher: PacketDispatcher
) : OutgoingConnectable by packetDispatcher {

    fun sendMessage(message: OutgoingMainMessage): Completable =
        messageSerializer.serialize(message).let {
            packetDispatcher.dispatch(it)
        }
}
