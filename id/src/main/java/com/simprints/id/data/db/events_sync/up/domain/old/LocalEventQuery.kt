package com.simprints.id.data.db.events_sync.up.domain.old

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.EventType


@Keep
@Deprecated(message = "This is used to support old data-upload format, should not be used going forward")
data class LocalEventQuery(val projectId: String? = null,
                           val id: String? = null,
                           val type: EventType? = null,
                           val subjectId: String? = null,
                           val attendantId: String? = null,
                           val sessionId: String? = null,
                           val deviceId: String? = null,
                           val startTime: LongRange? = null,
                           val endTime: LongRange? = null)
