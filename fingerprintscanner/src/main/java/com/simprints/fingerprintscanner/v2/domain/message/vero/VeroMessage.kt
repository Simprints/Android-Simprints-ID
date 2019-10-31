package com.simprints.fingerprintscanner.v2.domain.message.vero

import com.simprints.fingerprintscanner.v2.domain.message.IncomingMessage
import com.simprints.fingerprintscanner.v2.domain.message.Message
import com.simprints.fingerprintscanner.v2.domain.message.OutgoingMessage
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

sealed class VeroMessage(val veroMessageType: VeroMessageType) : Message {

    override fun getBytes(): ByteArray =
        VeroMessageProtocol.buildMessageBytes(veroMessageType, getDataBytes())

    open fun getDataBytes() = byteArrayOf()
}

abstract class VeroCommand(veroMessageType: VeroMessageType) : VeroMessage(veroMessageType), OutgoingMessage

abstract class VeroResponse(veroMessageType: VeroMessageType) : VeroMessage(veroMessageType), IncomingMessage

abstract class VeroEvent(veroMessageType: VeroMessageType) : VeroMessage(veroMessageType), IncomingMessage
