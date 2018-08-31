package com.simprints.id.data.analytics.eventData.models.events

import com.simprints.id.data.analytics.eventData.models.EventType

class OneToOneMatchEvent(val relativeStartTime: Long,
                         val relativeEndTime: Long,
                         val candidateId: String,
                         val result: MatchCandidate?) : Event(EventType.ONE_TO_ONE_MATCH)
