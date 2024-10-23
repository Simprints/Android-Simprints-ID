package com.simprints.infra.events.event.domain.validators

import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.events.exceptions.validator.PersonCreationEventException

internal class PersonCreationEventValidator : EventValidator {

    /**
     * This validator checks to make sure that no more than the allowed number of PersonCreation events
     * are added to the session.
     * "Normal" sessions have only one PersonCreation event.
     * Sessions that skip a modality (due to Matching Modalities configuration) have two PersonCreation events.
     */
    override fun validate(currentEvents: List<Event>, eventToAdd: Event) {
        if (eventToAdd is PersonCreationEvent) {
            val existingPersonCreationEventsCount = currentEvents
                .filter { it is PersonCreationEvent }
                .count { it.id != eventToAdd.id }
            if (existingPersonCreationEventsCount > 1) {
                throw PersonCreationEventException("The session already has the maximum PersonCreationEvents allowed ($existingPersonCreationEventsCount)")
            }
        }
    }

}
