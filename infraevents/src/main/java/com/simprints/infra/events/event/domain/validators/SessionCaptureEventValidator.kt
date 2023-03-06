package com.simprints.infra.events.domain.validators

import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.infra.events.event.domain.models.session.SessionCaptureEvent
import com.simprints.infra.events.exceptions.validator.SessionEventCaptureAlreadyExists

internal class SessionCaptureEventValidator : EventValidator {

    /**
     * This validator checks to make sure that no new SESSION_CAPTURE events are added to the session.
     * There can only be one session capture event in any given session.
     */
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
