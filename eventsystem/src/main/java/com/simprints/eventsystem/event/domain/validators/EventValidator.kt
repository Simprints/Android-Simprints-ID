package com.simprints.eventsystem.event.domain.validators

import com.simprints.eventsystem.event.domain.models.Event

internal interface EventValidator {

    fun validate(currentEvents: List<Event>, eventToAdd: Event)

}
