package com.simprints.feature.clientapi.mappers.request.validators

import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.extractors.ConfirmIdentityRequestExtractor
import com.simprints.feature.clientapi.models.ClientApiError
import com.simprints.infra.config.store.models.experimental
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.callback.IdentificationCallbackEvent
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SESSION
import com.simprints.infra.logging.Simber

internal class ConfirmIdentityValidator(
    private val extractor: ConfirmIdentityRequestExtractor,
    private val currentSessionId: String,
    private val eventRepository: EventRepository,
    private val configManager: ConfigManager,
) : RequestActionValidator(extractor) {
    private var identificationEvent: IdentificationCallbackEvent? = null

    override suspend fun validate() {
        validateProjectId()
        validateSessionId(extractor.getSessionId())
        validateSessionEvents(extractor.getSessionId())
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

    private suspend fun validateSessionEvents(sessionId: String) {
        identificationEvent = eventRepository
            .getEventsFromScope(sessionId)
            .filterIsInstance<IdentificationCallbackEvent>()
            .lastOrNull()

        if (identificationEvent == null) {
            throw InvalidRequestException(
                "Calling app wants to confirm identity, but the session doesn't have an identification callback event.",
                ClientApiError.INVALID_SESSION_ID,
            )
        }
    }

    private suspend fun validateSelectedGuid(selectedId: String) {
        if (selectedId.isBlank()) {
            throw InvalidRequestException("Missing Selected GUID", ClientApiError.INVALID_SELECTED_ID)
        }

        // Allow 'NONE_SELECTED' as a special case to indicate no selection
        if (selectedId.equals("NONE_SELECTED", ignoreCase = true)) {
            return
        }

        // Skip further validation if skip flag is enabled
        if (configManager.getProjectConfiguration().experimental().allowConfirmingGuidsNotInCallback) {
            return
        }

        val validGuids = identificationEvent?.payload?.scores?.map { it.guid } ?: emptyList()

        if (validGuids.isEmpty()) {
            throw InvalidRequestException(
                "No identification results found in session",
                ClientApiError.INVALID_SELECTED_ID,
            )
        }

        if (!validGuids.contains(selectedId)) {
            Simber.i("Selected GUID '$selectedId' not found in identification results: $validGuids", tag = SESSION)
            throw InvalidRequestException(
                "Selected GUID was not part of the identification results",
                ClientApiError.INVALID_SELECTED_ID,
            )
        }
    }
}
