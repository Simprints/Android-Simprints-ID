package com.simprints.fingerprintscanner.v2.incoming.message.parsers

import com.simprints.fingerprintscanner.v2.domain.message.Message

interface MessageParser {

    fun parse(bytes: ByteArray): Message
}
