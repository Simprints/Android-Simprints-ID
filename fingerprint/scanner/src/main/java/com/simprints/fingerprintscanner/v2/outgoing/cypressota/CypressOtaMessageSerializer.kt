package com.simprints.fingerprintscanner.v2.outgoing.cypressota

import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaCommand
import com.simprints.fingerprintscanner.v2.outgoing.common.MessageSerializer

class CypressOtaMessageSerializer : MessageSerializer<CypressOtaCommand> {

    override fun serialize(message: CypressOtaCommand): List<ByteArray> =
        listOf(message.getBytes())
}
