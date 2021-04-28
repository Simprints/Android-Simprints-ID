package com.simprints.id.data.db.event.domain.validators

import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent

interface EventValidator {

    fun validate(currentEvents: List<Event>, eventToAdd: Event)

}
