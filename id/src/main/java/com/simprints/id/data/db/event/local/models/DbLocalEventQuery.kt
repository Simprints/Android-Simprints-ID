package com.simprints.id.data.db.event.local.models

import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.events_sync.up.domain.LocalEventQuery

data class DbLocalEventQuery(val id: String? = null,
                             val type: EventType? = null,
                             val projectId: String? = null,
                             val subjectId: String? = null,
                             val attendantId: String? = null,
                             val sessionId: String? = null,
                             val deviceId: String? = null,
                             val startTime: LongRange? = null,
                             val endTime: LongRange? = null)

fun LocalEventQuery.fromDomainToDb() =
    DbLocalEventQuery(
        id,
        type,
        projectId,
        subjectId,
        attendantId,
        sessionId,
        deviceId,
        startTime,
        endTime
    )
