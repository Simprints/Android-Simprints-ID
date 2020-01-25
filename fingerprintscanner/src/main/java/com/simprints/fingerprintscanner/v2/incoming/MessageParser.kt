package com.simprints.fingerprintscanner.v2.incoming

import com.simprints.fingerprintscanner.v2.domain.Message
import com.simprints.fingerprintscanner.v2.exceptions.parsing.InvalidMessageException

interface MessageParser<out R : Message> {

    /** @throws InvalidMessageException */
    fun parse(messageBytes: ByteArray): R
}
