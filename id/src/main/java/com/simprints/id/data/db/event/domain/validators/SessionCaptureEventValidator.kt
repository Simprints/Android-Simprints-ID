package com.simprints.id.data.db.event.domain.validators

import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.exceptions.safe.session.validator.SessionEventCaptureAlreadyExists

class SessionCaptureEventValidator : EventValidator {

    override fun validate(currentSession: SessionCaptureEvent, eventToAdd: Event) {
        if (eventToAdd is SessionCaptureEvent) {
            if (currentSession.id != eventToAdd.id) {
                throw SessionEventCaptureAlreadyExists("The session already has a SessionCaptureEvent")
            }
        }
    }
}
