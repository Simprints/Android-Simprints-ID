package com.simprints.infra.events.event.local

import com.simprints.infra.events.event.domain.models.Event

internal interface SessionDataCache {

    val eventCache: MutableMap<String, Event>

}
