package com.simprints.id.data.db.event.domain.validators

import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_IDENTIFICATION
import com.simprints.id.data.db.event.domain.models.EventType.GUID_SELECTION
import com.simprints.id.data.db.event.domain.models.GuidSelectionEvent
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.exceptions.safe.session.validator.GuidSelectEventValidatorException

class GuidSelectionEventValidator : EventValidator {

    override fun validate(currentSession: SessionCaptureEvent, eventToAdd: Event) {
        if (eventToAdd is GuidSelectionEvent) {

            if (currentSession.payload.validators.any { it == GUID_SELECTION }) {
                throw GuidSelectEventValidatorException("GuidSelectionEvent already exist in session ${currentSession.id}")
            }

            if (currentSession.payload.validators.none { it == CALLBACK_IDENTIFICATION })
                throw GuidSelectEventValidatorException("Identification Callback missing in session ${currentSession.id}")
        }
    }
}
