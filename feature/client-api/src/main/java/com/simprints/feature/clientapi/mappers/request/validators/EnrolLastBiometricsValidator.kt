package com.simprints.feature.clientapi.mappers.request.validators

import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.extractors.EnrolLastBiometricsRequestExtractor
import com.simprints.feature.clientapi.models.ClientApiError
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SESSION
import com.simprints.infra.logging.Simber

internal class EnrolLastBiometricsValidator(
    private val extractor: EnrolLastBiometricsRequestExtractor,
    private val currentSessionId: String,
    private val sessionHasIdentificationCallback: Boolean,
) : RequestActionValidator(extractor) {
    override fun validate() {
        super.validate()
        validateSessionId(extractor.getSessionId())
        validateSessionEvents()
    }

    private fun validateSessionId(sessionId: String) {
        if (sessionId.isBlank()) {
            throw InvalidRequestException("Missing Session ID", ClientApiError.INVALID_SESSION_ID)
        }
        if (currentSessionId != sessionId) {
            Simber.i("Mismatched IDs: '$currentSessionId' != '$sessionId'", tag = SESSION)
            throw InvalidRequestException("Invalid Session ID", ClientApiError.INVALID_SESSION_ID)
        }
    }

    private fun validateSessionEvents() {
        if (!sessionHasIdentificationCallback) {
            throw InvalidRequestException(
                "Calling app wants to enrol last biometrics, but the session doesn't have an identification callback event.",
                ClientApiError.INVALID_STATE_FOR_INTENT_ACTION,
            )
        }
    }
}
