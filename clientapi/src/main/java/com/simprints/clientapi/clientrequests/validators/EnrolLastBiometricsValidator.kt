package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.clientrequests.extractors.EnrolLastBiometricsExtractor
import com.simprints.clientapi.exceptions.InvalidSessionIdException
import com.simprints.clientapi.exceptions.InvalidStateForIntentAction

class EnrolLastBiometricsValidator(val extractor: EnrolLastBiometricsExtractor,
                                   private val currentSession: String,
                                   private val isCurrentSessionAnEnrolmentOrIdentification: Boolean)
    : ClientRequestValidator(extractor) {

    override fun validateClientRequest() {
        validateProjectId()
        validateSessionId(extractor.getSessionId())
        if(!isCurrentSessionAnEnrolmentOrIdentification) {
            throw InvalidStateForIntentAction("Calling app wants to enrol last biometrics, but last flow was not an identification.")
        }
    }

    private fun validateSessionId(sessionId: String) {
        if (sessionId.isBlank())
            throw InvalidSessionIdException("Missing Session ID")

        if(currentSession != sessionId)
            throw InvalidSessionIdException("Invalid Session ID")

    }
}
