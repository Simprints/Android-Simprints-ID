package com.simprints.id.data.db.event.local

import com.simprints.id.data.db.event.domain.models.Event

interface SessionDataCache {

    val eventCache: MutableMap<String, Event>

}
