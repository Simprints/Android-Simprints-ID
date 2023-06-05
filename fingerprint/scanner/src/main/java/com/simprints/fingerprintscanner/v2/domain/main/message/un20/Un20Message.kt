package com.simprints.fingerprintscanner.v2.domain.main.message.un20

import com.simprints.fingerprintscanner.v2.domain.main.message.IncomingMainMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.MainMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.OutgoingMainMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20MessageType

sealed class Un20Message(val un20MessageType: Un20MessageType) : MainMessage {

    override fun getBytes(): ByteArray =
        Un20MessageProtocol.buildMessageBytes(un20MessageType, getDataBytes())

    open fun getDataBytes() = byteArrayOf()
}

abstract class Un20Command(un20MessageType: Un20MessageType) : Un20Message(un20MessageType), OutgoingMainMessage

abstract class Un20Response(un20MessageType: Un20MessageType) : Un20Message(un20MessageType), IncomingMainMessage
