package com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.responses

import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaResponseType

class ErrorResponse : CypressOtaResponse(CypressOtaResponseType.ERROR)
