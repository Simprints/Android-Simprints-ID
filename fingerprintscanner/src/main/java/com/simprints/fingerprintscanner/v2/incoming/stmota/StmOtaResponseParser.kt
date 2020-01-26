package com.simprints.fingerprintscanner.v2.incoming.stmota

import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaResponse
import com.simprints.fingerprintscanner.v2.domain.stmota.responses.CommandAcknowledgement
import com.simprints.fingerprintscanner.v2.incoming.common.MessageParser

class StmOtaResponseParser : MessageParser<StmOtaResponse> {

    override fun parse(messageBytes: ByteArray): StmOtaResponse =
        try {
            CommandAcknowledgement.fromBytes(messageBytes)
        } catch (e: Exception) {
            handleExceptionDuringParsing(e)
        }
}
