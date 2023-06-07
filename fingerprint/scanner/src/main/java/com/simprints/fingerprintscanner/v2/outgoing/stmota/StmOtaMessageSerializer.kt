package com.simprints.fingerprintscanner.v2.outgoing.stmota

import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaCommand
import com.simprints.fingerprintscanner.v2.outgoing.common.MessageSerializer

class StmOtaMessageSerializer : MessageSerializer<StmOtaCommand> {

    override fun serialize(message: StmOtaCommand): List<ByteArray> =
        listOf(message.getBytes())
}
