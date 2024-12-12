package com.simprints.feature.clientapi.mappers.request.validators

import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.extractors.EnrolLastBiometricsRequestExtractor
import com.simprints.feature.clientapi.models.ClientApiError

internal class EnrolLastBiometricsValidator(
    private val extractor: EnrolLastBiometricsRequestExtractor,
    private val currentSession: String,
    private val isCurrentSessionAnEnrolmentOrIdentification: Boolean,
) : RequestActionValidator(extractor) {
    override fun validate() {
        super.validate()
        validateSessionId(extractor.getSessionId())
        if (!isCurrentSessionAnEnrolmentOrIdentification) {
            throw InvalidRequestException(
                "Calling app wants to enrol last biometrics, but last flow was not an identification.",
                ClientApiError.INVALID_STATE_FOR_INTENT_ACTION,
            )
        }
    }

    private fun validateSessionId(sessionId: String) {
        if (sessionId.isBlank()) {
            throw InvalidRequestException("Missing Session ID", ClientApiError.INVALID_SESSION_ID)
        }

        if (currentSession != sessionId) {
            throw InvalidRequestException("Invalid Session ID", ClientApiError.INVALID_SESSION_ID)
        }
    }
}
