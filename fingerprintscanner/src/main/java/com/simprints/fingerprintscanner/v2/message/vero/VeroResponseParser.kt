package com.simprints.fingerprintscanner.v2.message.vero

import com.simprints.fingerprintscanner.v2.message.Message
import com.simprints.fingerprintscanner.v2.message.MessageParser

class VeroResponseParser: MessageParser {

    override fun parse(bytes: ByteArray): Message = Message(bytes)
}
