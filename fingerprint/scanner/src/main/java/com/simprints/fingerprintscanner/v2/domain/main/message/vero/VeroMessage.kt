package com.simprints.fingerprintscanner.v2.domain.main.message.vero

import com.simprints.fingerprintscanner.v2.domain.main.message.IncomingMainMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.MainMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.OutgoingMainMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.VeroMessageType

sealed class VeroMessage(val veroMessageType: VeroMessageType) : MainMessage {

    override fun getBytes(): ByteArray =
        VeroMessageProtocol.buildMessageBytes(veroMessageType, getDataBytes())

    open fun getDataBytes() = byteArrayOf()
}

abstract class VeroCommand(veroMessageType: VeroMessageType) : VeroMessage(veroMessageType), OutgoingMainMessage

abstract class VeroResponse(veroMessageType: VeroMessageType) : VeroMessage(veroMessageType), IncomingMainMessage

abstract class VeroEvent(veroMessageType: VeroMessageType) : VeroMessage(veroMessageType), IncomingMainMessage
