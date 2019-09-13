package com.simprints.fingerprintscanner.v2.incoming.message.parsers

import com.simprints.fingerprintscanner.v2.domain.message.IncomingMessage

interface MessageParser<out R: IncomingMessage> {

    fun parse(bytes: ByteArray): R
}
