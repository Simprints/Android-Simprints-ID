package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class OneToOneMatchEvent(val relativeStartTime: Long,
                         val relativeEndTime: Long,
                         val candidateId: String,
                         val result: MatchEntry?) : Event(EventType.ONE_TO_ONE_MATCH)
