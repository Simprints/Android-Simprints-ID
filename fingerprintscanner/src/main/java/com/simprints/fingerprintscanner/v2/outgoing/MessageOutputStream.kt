package com.simprints.fingerprintscanner.v2.outgoing

import com.simprints.fingerprintscanner.v2.domain.message.Message
import com.simprints.fingerprintscanner.v2.outgoing.message.MessageSerializer
import com.simprints.fingerprintscanner.v2.outgoing.packet.PacketDispatcher
import io.reactivex.Completable

class MessageOutputStream(
    private val messageSerializer: MessageSerializer,
    private val packetDispatcher: PacketDispatcher
) : OutgoingConnectable by packetDispatcher {

    fun sendMessage(message: Message): Completable =
        messageSerializer.serialize(message).let {
            packetDispatcher.dispatch(it)
        }
}
