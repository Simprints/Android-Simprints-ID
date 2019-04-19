package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.OneToManyMatchEvent

@Keep
class ApiOneToManyMatchEvent(val relativeStartTime: Long,
                             val relativeEndTime: Long,
                             val pool: ApiMatchPool,
                             val result: List<ApiMatchEntry>?): ApiEvent(ApiEventType.ONE_TO_MANY_MATCH) {

    @Keep
    class ApiMatchPool(val type: ApiMatchPoolType, val count: Int) {
        constructor(matchPool: OneToManyMatchEvent.MatchPool) :
            this(ApiMatchPoolType.valueOf(matchPool.type.toString()), matchPool.count)
    }

    @Keep
    enum class ApiMatchPoolType {
        USER,
        MODULE,
        PROJECT;
    }

    constructor(oneToManyMatchEvent: OneToManyMatchEvent) :
        this(oneToManyMatchEvent.relativeStartTime,
            oneToManyMatchEvent.relativeEndTime,
            ApiMatchPool(oneToManyMatchEvent.pool),
            oneToManyMatchEvent.result?.map { ApiMatchEntry(it) })
}
