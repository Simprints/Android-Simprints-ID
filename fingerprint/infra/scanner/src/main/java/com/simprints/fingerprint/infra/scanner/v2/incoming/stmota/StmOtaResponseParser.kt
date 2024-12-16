package com.simprints.fingerprint.infra.scanner.v2.incoming.stmota

import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.StmOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.responses.CommandAcknowledgement
import com.simprints.fingerprint.infra.scanner.v2.incoming.common.MessageParser
import javax.inject.Inject

class StmOtaResponseParser @Inject constructor() : MessageParser<StmOtaResponse> {
    override fun parse(messageBytes: ByteArray): StmOtaResponse = try {
        CommandAcknowledgement.fromBytes(messageBytes)
    } catch (e: Exception) {
        handleExceptionDuringParsing(e)
    }
}
