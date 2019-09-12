package com.simprints.fingerprintscanner.v2.incoming.message.parsers

import com.simprints.fingerprintscanner.v2.domain.message.Message

class VeroEventParser: MessageParser {

    override fun parse(bytes: ByteArray): Message = Message(bytes)
}
