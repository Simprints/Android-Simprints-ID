package com.simprints.id.data.db.events_sync.up.domain

import com.simprints.id.data.db.event.domain.models.EventType

data class LocalEventQuery(val projectId: String? = null,
                           val id: String? = null,
                           val type: EventType? = null,
                           val subjectId: String? = null,
                           val attendantId: String? = null,
                           val sessionId: String? = null,
                           val deviceId: String? = null,
                           val startTime: LongRange? = null,
                           val endTime: LongRange? = null)
