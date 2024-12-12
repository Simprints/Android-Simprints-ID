package com.simprints.infra.events.session

import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.scope.EventScope
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SessionDataCache @Inject constructor() {
    private val scopeLock = Any()

    var eventScope: EventScope? = null
        get() = synchronized(scopeLock) { field }
        set(value) = synchronized(scopeLock) { field = value }

    // If multiple threads access a linked hash map concurrently,
    // and at least one of the threads modifies the map structurally,
    // a ConcurrentModificationException will happen.
    // In access-ordered linked hash maps, merely querying the map with get is a structural modification.
    // We should use ConcurrentHashMap instead mutableMapOf which creates an empty linkedhashmap
    val eventCache: MutableMap<String, Event> = ConcurrentHashMap()
}
