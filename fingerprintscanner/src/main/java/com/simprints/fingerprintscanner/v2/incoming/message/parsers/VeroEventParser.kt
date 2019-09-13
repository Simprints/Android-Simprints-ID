package com.simprints.fingerprintscanner.v2.incoming.message.parsers

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroEvent

class VeroEventParser: MessageParser<VeroEvent> {

    override fun parse(bytes: ByteArray): VeroEvent = TODO()
}
