package com.simprints.fingerprint.infra.scanner.v2.incoming.cypressota

import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaResponseType
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaResponseType.CONTINUE
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaResponseType.ERROR
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaResponseType.OK
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.responses.ContinueResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.responses.ErrorResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.responses.OkResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.common.MessageParser
import javax.inject.Inject

class CypressOtaResponseParser @Inject constructor() : MessageParser<CypressOtaResponse> {
    override fun parse(messageBytes: ByteArray): CypressOtaResponse = try {
        when (CypressOtaResponseType.fromByte(messageBytes[0])) {
            OK -> OkResponse()
            CONTINUE -> ContinueResponse()
            ERROR -> ErrorResponse()
        }
    } catch (e: Exception) {
        handleExceptionDuringParsing(e)
    }
}
