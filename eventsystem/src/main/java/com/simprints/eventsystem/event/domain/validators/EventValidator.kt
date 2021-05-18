package com.simprints.eventsystem.event.domain.validators

import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.session.SessionCaptureEvent

interface EventValidator {

    fun validate(currentEvents: List<Event>, eventToAdd: Event)

}
