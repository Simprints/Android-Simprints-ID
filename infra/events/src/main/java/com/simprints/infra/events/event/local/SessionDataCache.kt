package com.simprints.infra.events.event.local

import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.scope.EventScope

internal interface SessionDataCache {

    var eventScope: EventScope?

    val eventCache: MutableMap<String, Event>

}
