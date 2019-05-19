package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class OneToManyMatchEvent(override val starTime: Long,
                          override val endTime: Long,
                          val pool: MatchPool,
                          val result: List<MatchEntry>?) : Event(EventType.ONE_TO_MANY_MATCH) {

    @Keep
    class MatchPool(val type: MatchPoolType, val count: Int)

    @Keep
    enum class MatchPoolType {
        USER,
        MODULE,
        PROJECT;
    }
}
