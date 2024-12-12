package com.simprints.infra.events.event.domain.validators

import com.simprints.infra.events.event.domain.models.Event

internal interface EventValidator {
    fun validate(
        currentEvents: List<Event>,
        eventToAdd: Event,
    )
}
