package com.simprints.feature.clientapi.mappers.request.validators

import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.extractors.ConfirmIdentityRequestExtractor
import com.simprints.feature.clientapi.models.ClientApiError
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SESSION
import com.simprints.infra.logging.Simber

internal class ConfirmIdentityValidator(
    private val extractor: ConfirmIdentityRequestExtractor,
    private val currentSessionId: String,
    private val sessionHasIdentificationCallback: Boolean,
) : RequestActionValidator(extractor) {
    override fun validate() {
        validateProjectId()
        validateSessionId(extractor.getSessionId())
        validateSessionEvents()
        validateSelectedGuid(extractor.getSelectedGuid())
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
                "Calling app wants to confirm identity, but the session doesn't have an identification callback event.",
                ClientApiError.INVALID_SESSION_ID,
            )
        }
    }

    private fun validateSelectedGuid(selectedId: String) {
        if (selectedId.isBlank()) {
            throw InvalidRequestException("Missing Selected GUID", ClientApiError.INVALID_SELECTED_ID)
        }
    }
}
