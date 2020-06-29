package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class OneToOneMatchEvent(
    startTime: Long,
    endTime: Long,
    val candidateId: String,
    val matcher: Matcher,
    val result: MatchEntry?
) : Event(EventType.ONE_TO_ONE_MATCH, startTime, endTime)
