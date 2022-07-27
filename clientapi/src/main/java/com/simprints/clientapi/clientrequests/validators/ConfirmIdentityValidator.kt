package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentityExtractor
import com.simprints.clientapi.exceptions.InvalidSelectedIdException
import com.simprints.clientapi.exceptions.InvalidSessionIdException
import com.simprints.clientapi.exceptions.InvalidStateForIntentAction


class ConfirmIdentityValidator(val extractor: ConfirmIdentityExtractor,
                               private val currentSessionId: String,
                               private val isSessionHasIdentificationCallback: Boolean)
    : ClientRequestValidator(extractor) {

    override fun validateClientRequest() {
        validateProjectId()
        validateSessionId(extractor.getSessionId())
        validateSelectedGuid(extractor.getSelectedGuid())
        validateSessionEvents()
    }

    private fun validateSessionEvents() {
        if(!isSessionHasIdentificationCallback) {
            throw InvalidStateForIntentAction(
                "Calling app wants to confirm identity, but the session doesn't have an identification callback event.")
        }
    }

    private fun validateSessionId(sessionId: String) {
        if (sessionId.isBlank())
            throw InvalidSessionIdException("Missing Session ID")
        if(currentSessionId != sessionId)
            throw InvalidSessionIdException("Invalid Session ID")

    }

    private fun validateSelectedGuid(selectedId: String) {
        if (selectedId.isBlank())
            throw InvalidSelectedIdException("Missing Selected GUID")
    }

}
