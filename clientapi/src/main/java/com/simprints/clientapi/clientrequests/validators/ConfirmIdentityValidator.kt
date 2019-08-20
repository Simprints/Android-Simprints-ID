package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentityExtractor
import com.simprints.clientapi.exceptions.InvalidSelectedIdException
import com.simprints.clientapi.exceptions.InvalidSessionIdException


class ConfirmIdentityValidator(val extractor: ConfirmIdentityExtractor)
    : ClientRequestValidator(extractor) {

    override fun validateClientRequest() {
        validateProjectId()
        validateSessionId(extractor.getSessionId())
        validateSelectedGuid(extractor.getSelectedGuid())
    }

    private fun validateSessionId(sessionId: String) {
        if (sessionId.isBlank())
            throw InvalidSessionIdException("Missing Session ID")
    }

    private fun validateSelectedGuid(selectedId: String) {
        if (selectedId.isBlank())
            throw InvalidSelectedIdException("Missing Selected GUID")
    }

}
