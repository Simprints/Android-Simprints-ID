package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.clientrequests.extractors.EnrolLastBiometricsExtractor
import com.simprints.clientapi.exceptions.InvalidSessionIdException

class EnrolLastBiometricsValidator(val extractor: EnrolLastBiometricsExtractor)
    : ClientRequestValidator(extractor) {

    override fun validateClientRequest() {
        validateProjectId()
        validateSessionId(extractor.getSessionId())
    }

    private fun validateSessionId(sessionId: String) {
        if (sessionId.isBlank())
            throw InvalidSessionIdException("Missing Session ID")
    }
}
