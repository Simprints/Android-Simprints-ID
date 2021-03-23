package com.simprints.id.data.db.event.domain.validators

import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventType.PERSON_CREATION
import com.simprints.id.data.db.event.domain.models.PersonCreationEvent
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.exceptions.safe.session.validator.SessionEventCaptureAlreadyExists

class PersonCreationEventValidator : EventValidator {

    override fun validate(currentSession: SessionCaptureEvent, eventToAdd: Event) {
        if (eventToAdd is PersonCreationEvent) {
            if (currentSession.payload.validators.any { it == PERSON_CREATION }) {
                throw SessionEventCaptureAlreadyExists("The session already has a PersonCreationEvent")
            }
        }
    }
}
