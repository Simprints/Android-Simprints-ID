package com.simprints.id.data.analytics.events.models

class OneToOneMatchEvent(val relativeStartTime: Long,
                         val relativeEndTime: Long,
                         val candidateId: String,
                         val result: MatchCandidate?) : Event(EventType.ONE_TO_ONE_MATCH)
