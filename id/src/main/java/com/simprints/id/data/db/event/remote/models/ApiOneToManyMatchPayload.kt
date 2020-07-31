package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload
import com.simprints.id.data.db.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPool
import com.simprints.id.data.db.event.remote.models.face.ApiMatcher
import com.simprints.id.data.db.event.remote.models.face.fromDomainToApi

@Keep
class ApiOneToManyMatchPayload(override val relativeStartTime: Long,
                               override val version: Int,
                               val relativeEndTime: Long,
                               val pool: ApiMatchPool,
                               val matcher: ApiMatcher,
                               val result: List<ApiMatchEntry>?) : ApiEventPayload(ApiEventPayloadType.ONE_TO_MANY_MATCH,version, relativeStartTime) {

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

    constructor(domainPayload: OneToManyMatchPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.endedAt,
            ApiMatchPool(domainPayload.pool),
            domainPayload.matcher.fromDomainToApi(),
            domainPayload.result?.map { ApiMatchEntry(it) })
}
