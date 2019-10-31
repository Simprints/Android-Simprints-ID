package com.simprints.fingerprintscanner.v2.incoming.message.parsers

import com.simprints.fingerprintscanner.v2.domain.message.Message

interface MessageParser<out R: Message> {

    fun parse(messageBytes: ByteArray): R
}
