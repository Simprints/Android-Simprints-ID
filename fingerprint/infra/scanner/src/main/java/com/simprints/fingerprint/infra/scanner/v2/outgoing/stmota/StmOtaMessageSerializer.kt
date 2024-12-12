package com.simprints.fingerprint.infra.scanner.v2.outgoing.stmota

import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.StmOtaCommand
import com.simprints.fingerprint.infra.scanner.v2.outgoing.common.MessageSerializer
import javax.inject.Inject

class StmOtaMessageSerializer @Inject constructor() : MessageSerializer<StmOtaCommand> {
    override fun serialize(message: StmOtaCommand): List<ByteArray> = listOf(message.getBytes())
}
