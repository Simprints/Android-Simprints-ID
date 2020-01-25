package com.simprints.fingerprintscanner.v2.outgoing.root

import com.simprints.fingerprintscanner.v2.domain.root.RootCommand
import com.simprints.fingerprintscanner.v2.domain.root.RootMessageProtocol
import com.simprints.fingerprintscanner.v2.outgoing.MessageSerializer
import com.simprints.fingerprintscanner.v2.tools.primitives.chunked

class RootMessageSerializer : MessageSerializer<RootCommand> {

    override fun serialize(message: RootCommand): List<ByteArray> =
        message.getBytes().chunked(RootMessageProtocol.MAX_PAYLOAD_SIZE)
}
