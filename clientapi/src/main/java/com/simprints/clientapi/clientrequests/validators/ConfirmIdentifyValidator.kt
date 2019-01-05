package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentifyExtractor
import com.simprints.clientapi.exceptions.InvalidSessionIdException


class ConfirmIdentifyValidator(val extractor: ConfirmIdentifyExtractor)
    : ClientRequestValidator(extractor) {

    override fun validateClientRequest() {
        super.validateClientRequest()
        validateSessionId(extractor.getSessionId())
    }

    private fun validateSessionId(sessionId: String?) {
        if (sessionId.isNullOrBlank())
            throw InvalidSessionIdException("Missing Session ID")
    }

}
