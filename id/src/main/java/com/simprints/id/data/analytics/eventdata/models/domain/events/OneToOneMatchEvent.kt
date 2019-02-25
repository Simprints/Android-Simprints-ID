package com.simprints.id.data.analytics.eventData.models.domain.events

import com.simprints.id.data.analytics.eventData.models.domain.EventType

class OneToOneMatchEvent(val relativeStartTime: Long,
                         val relativeEndTime: Long,
                         val candidateId: String,
                         val result: MatchEntry?) : Event(EventType.ONE_TO_ONE_MATCH)
