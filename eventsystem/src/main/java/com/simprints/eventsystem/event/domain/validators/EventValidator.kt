package com.simprints.eventsystem.event.domain.validators

import com.simprints.eventsystem.event.domain.models.Event

interface EventValidator {

    fun validate(currentEvents: List<Event>, eventToAdd: Event)

}
