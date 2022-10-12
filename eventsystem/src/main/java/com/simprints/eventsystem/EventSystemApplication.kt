package com.simprints.eventsystem

import com.simprints.core.CoreApplication
import com.simprints.eventsystem.event.domain.models.Event
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class EventSystemApplication @Inject constructor(): CoreApplication() {

    // If multiple threads access a linked hash map concurrently,
    // and at least one of the threads modifies the map structurally,
    // a ConcurrentModificationException will happen.
    // In access-ordered linked hash maps, merely querying the map with get is a structural modification.
    // We should use ConcurrentHashMap instead mutableMapOf which creates an empty linkedhashmap

    val eventCache: MutableMap<String, Event> = ConcurrentHashMap()

}
