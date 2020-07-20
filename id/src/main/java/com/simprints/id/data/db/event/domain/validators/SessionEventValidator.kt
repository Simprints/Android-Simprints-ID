package com.simprints.id.data.db.event.domain.validators

import com.simprints.id.data.db.event.domain.models.Event

interface SessionEventValidator {

    fun validate(currentEvents: List<Event>, eventToAdd: Event)

}
