package com.simprints.fingerprint.infra.scanner.v2.outgoing.root

import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.outgoing.common.MessageSerializer
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.chunked

class RootMessageSerializer : MessageSerializer<RootCommand> {

    override fun serialize(message: RootCommand): List<ByteArray> =
        message.getBytes().chunked(RootMessageProtocol.MAX_PAYLOAD_SIZE)
}
