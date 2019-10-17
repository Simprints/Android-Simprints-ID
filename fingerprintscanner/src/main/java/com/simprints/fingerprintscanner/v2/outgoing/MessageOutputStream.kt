package com.simprints.fingerprintscanner.v2.outgoing

import com.simprints.fingerprintscanner.v2.domain.message.OutgoingMessage
import com.simprints.fingerprintscanner.v2.outgoing.message.MessageSerializer
import com.simprints.fingerprintscanner.v2.outgoing.packet.PacketDispatcher
import com.simprints.fingerprintscanner.v2.tools.primitives.toHexString
import io.reactivex.Completable

class MessageOutputStream(
    private val messageSerializer: MessageSerializer,
    private val packetDispatcher: PacketDispatcher
) : OutgoingConnectable by packetDispatcher {

    fun sendMessage(message: OutgoingMessage): Completable =
        messageSerializer.serialize(message.also { println("Scanner Message Stream: OUT : ${it::class.simpleName} : ${it.getBytes().toHexString()}") }).let {
            packetDispatcher.dispatch(it)
        }
}
