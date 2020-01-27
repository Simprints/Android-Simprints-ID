package com.simprints.fingerprintscanner.v2.domain.root

import com.simprints.fingerprintscanner.v2.domain.IncomingMessage
import com.simprints.fingerprintscanner.v2.domain.Message
import com.simprints.fingerprintscanner.v2.domain.OutgoingMessage

sealed class RootMessage(val rootMessageType: RootMessageType) : Message {

    override fun getBytes(): ByteArray =
        RootMessageProtocol.buildMessageBytes(rootMessageType, getDataBytes())

    open fun getDataBytes() = byteArrayOf()
}

abstract class RootCommand(rootMessageType: RootMessageType) : RootMessage(rootMessageType), OutgoingMessage

abstract class RootResponse(rootMessageType: RootMessageType) : RootMessage(rootMessageType), IncomingMessage
