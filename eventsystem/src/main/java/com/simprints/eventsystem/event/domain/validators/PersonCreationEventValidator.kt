package com.simprints.eventsystem.event.domain.validators

import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventType.PERSON_CREATION
import com.simprints.eventsystem.event.domain.models.PersonCreationEvent
import com.simprints.eventsystem.exceptions.validator.SessionEventCaptureAlreadyExists

class PersonCreationEventValidator : EventValidator {

    /**
     * This validator checks to make sure that no new PERSON_CREATION events are added to the session.
     * There can only be one person creation event in any given session.
     */
    override fun validate(currentEvents: List<Event>, eventToAdd: Event) {
        if (eventToAdd is PersonCreationEvent) {
            currentEvents.filter { it.payload.type == PERSON_CREATION }.forEach {
                if (it.id != eventToAdd.id)
                    throw SessionEventCaptureAlreadyExists("The session already has a PersonCreationEvent")
            }
        }
    }

}
