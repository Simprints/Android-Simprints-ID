package com.simprints.fingerprintscanner.v2.incoming

import com.simprints.fingerprintscanner.v2.domain.Message

interface MessageParser<out R: Message> {

    fun parse(messageBytes: ByteArray): R
}
