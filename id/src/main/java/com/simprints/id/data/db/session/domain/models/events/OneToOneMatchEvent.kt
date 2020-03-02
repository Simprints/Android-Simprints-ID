package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class OneToOneMatchEvent(starTime: Long,
                         endTime: Long,
                         val candidateId: String,
                         val result: MatchEntry?) : Event(EventType.ONE_TO_ONE_MATCH, starTime, endTime)
