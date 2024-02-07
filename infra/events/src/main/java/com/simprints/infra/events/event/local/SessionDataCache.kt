package com.simprints.infra.events.event.local

import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.session.SessionScope

internal interface SessionDataCache {

    var sessionScope: SessionScope?

    val eventCache: MutableMap<String, Event>

}
