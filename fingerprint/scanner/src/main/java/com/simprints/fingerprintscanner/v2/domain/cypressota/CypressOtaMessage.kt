package com.simprints.fingerprintscanner.v2.domain.cypressota

import com.simprints.fingerprintscanner.v2.domain.IncomingMessage
import com.simprints.fingerprintscanner.v2.domain.Message
import com.simprints.fingerprintscanner.v2.domain.OutgoingMessage

sealed class CypressOtaMessage : Message {

    override fun getBytes(): ByteArray = byteArrayOf()

    open fun getDataBytes() = byteArrayOf()
}

abstract class CypressOtaCommand(val type: CypressOtaCommandType) : CypressOtaMessage(), OutgoingMessage {
    override fun getBytes(): ByteArray =
        CypressOtaMessageProtocol.buildMessageBytes(type, getDataBytes())
}

abstract class CypressOtaResponse(val type: CypressOtaResponseType) : CypressOtaMessage(), IncomingMessage
