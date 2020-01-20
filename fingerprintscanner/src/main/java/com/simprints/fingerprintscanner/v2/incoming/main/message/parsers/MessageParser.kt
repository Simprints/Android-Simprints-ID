package com.simprints.fingerprintscanner.v2.incoming.main.message.parsers

import com.simprints.fingerprintscanner.v2.domain.main.message.Message

interface MessageParser<out R: Message> {

    fun parse(messageBytes: ByteArray): R
}
