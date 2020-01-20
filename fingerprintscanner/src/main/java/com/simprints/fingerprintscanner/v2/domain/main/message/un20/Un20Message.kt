package com.simprints.fingerprintscanner.v2.domain.main.message.un20

import com.simprints.fingerprintscanner.v2.domain.main.message.IncomingMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.Message
import com.simprints.fingerprintscanner.v2.domain.main.message.OutgoingMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20MessageType

sealed class Un20Message(val un20MessageType: Un20MessageType) : Message {

    override fun getBytes(): ByteArray =
        Un20MessageProtocol.buildMessageBytes(un20MessageType, getDataBytes())

    open fun getDataBytes() = byteArrayOf()
}

abstract class Un20Command(un20MessageType: Un20MessageType) : Un20Message(un20MessageType), OutgoingMessage

abstract class Un20Response(un20MessageType: Un20MessageType) : Un20Message(un20MessageType), IncomingMessage
