package com.simprints.fingerprint.infra.scanner.v2.outgoing.cypressota

import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaCommand
import com.simprints.fingerprint.infra.scanner.v2.outgoing.common.MessageSerializer

class CypressOtaMessageSerializer : MessageSerializer<CypressOtaCommand> {

    override fun serialize(message: CypressOtaCommand): List<ByteArray> =
        listOf(message.getBytes())
}
