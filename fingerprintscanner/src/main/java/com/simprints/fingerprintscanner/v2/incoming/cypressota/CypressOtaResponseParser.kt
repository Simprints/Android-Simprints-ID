package com.simprints.fingerprintscanner.v2.incoming.cypressota

import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaResponse
import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaResponseType
import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaResponseType.*
import com.simprints.fingerprintscanner.v2.domain.cypressota.responses.ContinueResponse
import com.simprints.fingerprintscanner.v2.domain.cypressota.responses.ErrorResponse
import com.simprints.fingerprintscanner.v2.domain.cypressota.responses.OkResponse
import com.simprints.fingerprintscanner.v2.incoming.common.MessageParser

class CypressOtaResponseParser : MessageParser<CypressOtaResponse> {

    override fun parse(messageBytes: ByteArray): CypressOtaResponse =
        try {
            when (CypressOtaResponseType.fromByte(messageBytes[0])) {
                OK -> OkResponse()
                CONTINUE -> ContinueResponse()
                ERROR -> ErrorResponse()
            }
        } catch (e: Exception) {
            handleExceptionDuringParsing(e)
        }
}
