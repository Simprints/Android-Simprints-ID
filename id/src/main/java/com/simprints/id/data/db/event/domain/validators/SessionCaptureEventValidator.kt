package com.simprints.id.data.db.event.domain.validators

import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.exceptions.safe.session.validator.SessionEventCaptureAlreadyExists

class SessionCaptureEventValidator : EventValidator {
    override fun validate(currentEvents: List<Event>, eventToAdd: Event) {
        if (eventToAdd is SessionCaptureEvent) {
            currentEvents.filter { it.payload.type == SESSION_CAPTURE }.forEach {
                if (it.id != eventToAdd.id) {
                    throw SessionEventCaptureAlreadyExists("The session already has a SessionCaptureEvent")
                }
            }
        }
    }
}
