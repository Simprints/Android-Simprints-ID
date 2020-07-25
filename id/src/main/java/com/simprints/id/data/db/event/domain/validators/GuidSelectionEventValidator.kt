package com.simprints.id.data.db.event.domain.validators

import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventType.*
import com.simprints.id.data.db.event.domain.models.GuidSelectionEvent
import com.simprints.id.exceptions.safe.session.validator.GuidSelectEventValidatorException

class GuidSelectionEventValidator : EventValidator {

    override fun validate(currentEvents: List<Event>, eventToAdd: Event) {
        val currentSession = currentEvents.firstOrNull { it.payload.type == SESSION_CAPTURE }
        if (eventToAdd is GuidSelectionEvent) {

            if (currentEvents.count { it.payload.type == GUID_SELECTION } > 1) {
                throw GuidSelectEventValidatorException("GuidSelectionEvent already exist in session ${currentSession?.id}")
            }

            if (currentEvents.count { it.payload.type == CALLBACK_IDENTIFICATION } == 0)
                throw GuidSelectEventValidatorException("Identification Callback missing in session ${currentSession?.id}")
        }
    }
}
