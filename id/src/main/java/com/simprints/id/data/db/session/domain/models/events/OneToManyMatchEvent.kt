package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class OneToManyMatchEvent(
    startTime: Long,
    endTime: Long,
    val pool: MatchPool,
    val matcher: Matcher,
    val result: List<MatchEntry>?
) : Event(EventType.ONE_TO_MANY_MATCH, startTime, endTime) {

    @Keep
    class MatchPool(val type: MatchPoolType, val count: Int)

    @Keep
    enum class MatchPoolType {
        USER,
        MODULE,
        PROJECT;
    }
}
