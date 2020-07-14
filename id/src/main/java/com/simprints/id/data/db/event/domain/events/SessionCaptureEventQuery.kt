package com.simprints.id.data.db.event.domain.events

import com.simprints.id.data.db.event.domain.events.Event.EventLabel

sealed class EventQuery {

    data class byType(val type: EventPayloadType) : EventQuery()

    data class byLabel(val label: EventLabel) : EventQuery()

    data class byDate(val startedBefore: Long) : EventQuery()

}
