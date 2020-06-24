package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.OneToManyMatchEvent
import com.simprints.id.data.db.event.domain.events.OneToManyMatchEvent.OneToManyMatchPayload
import com.simprints.id.data.db.event.domain.events.OneToManyMatchEvent.OneToManyMatchPayload.MatchPool
import com.simprints.id.data.db.session.remote.events.ApiEvent

@Keep
class ApiOneToManyMatchEvent(val relativeStartTime: Long,
                             val relativeEndTime: Long,
                             val pool: ApiMatchPool,
                             val result: List<ApiMatchEntry>?) : ApiEvent(ApiEventType.ONE_TO_MANY_MATCH) {

    @Keep
    class ApiMatchPool(val type: ApiMatchPoolType, val count: Int) {
        constructor(matchPool: MatchPool) :
            this(ApiMatchPoolType.valueOf(matchPool.type.toString()), matchPool.count)
    }

    @Keep
    enum class ApiMatchPoolType {
        USER,
        MODULE,
        PROJECT;
    }

    constructor(oneToManyMatchEvent: OneToManyMatchEvent) :
        this((oneToManyMatchEvent.payload as OneToManyMatchPayload).relativeStartTime,
            oneToManyMatchEvent.payload.relativeEndTime,
            ApiMatchPool(oneToManyMatchEvent.payload.pool),
            oneToManyMatchEvent.payload.result?.map { ApiMatchEntry(it) })
}
