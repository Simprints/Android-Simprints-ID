package com.simprints.fingerprint.infra.scanner.v2.domain.stmota

import com.simprints.fingerprint.infra.scanner.v2.domain.IncomingMessage
import com.simprints.fingerprint.infra.scanner.v2.domain.Message
import com.simprints.fingerprint.infra.scanner.v2.domain.OutgoingMessage

sealed class StmOtaMessage : Message {
    override fun getBytes(): ByteArray = StmOtaMessageProtocol.buildMessageBytes(this)

    open fun getDataBytes() = byteArrayOf()
}

abstract class StmOtaCommand :
    StmOtaMessage(),
    OutgoingMessage

abstract class StmOtaResponse :
    StmOtaMessage(),
    IncomingMessage
