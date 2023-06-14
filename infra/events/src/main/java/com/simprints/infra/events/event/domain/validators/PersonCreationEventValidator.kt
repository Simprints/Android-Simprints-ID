package com.simprints.infra.events.domain.validators

import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType.PERSON_CREATION
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.events.exceptions.validator.SessionEventCaptureAlreadyExists

internal class PersonCreationEventValidator : EventValidator {

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
