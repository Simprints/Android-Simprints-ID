package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentityExtractor
import com.simprints.clientapi.exceptions.InvalidSelectedIdException
import com.simprints.clientapi.exceptions.InvalidSessionIdException
import com.simprints.clientapi.exceptions.InvalidStateForIntentAction


class ConfirmIdentityValidator(val extractor: ConfirmIdentityExtractor,
                               val isCurrentSessionAnEnrolmentOrIdentification: Boolean)
    : ClientRequestValidator(extractor) {

    override fun validateClientRequest() {
        validateProjectId()
        validateSessionId(extractor.getSessionId())
        validateSelectedGuid(extractor.getSelectedGuid())
        validateSessionType()
    }

    private fun validateSessionType() {
        if(!isCurrentSessionAnEnrolmentOrIdentification) {
            throw InvalidStateForIntentAction(
                "Calling app wants to confirm identity, but last flow was not an identification.")
        }
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
