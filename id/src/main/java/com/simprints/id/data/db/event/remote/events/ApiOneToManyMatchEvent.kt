package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.OneToManyMatchEvent
import com.simprints.id.data.db.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload
import com.simprints.id.data.db.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPool

@Keep
class ApiOneToManyMatchEvent(domainEvent: OneToManyMatchEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.map { it.fromDomainToApi() }.toMap(),
        domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiOneToManyMatchPayload(createdAt: Long,
                                   eventVersion: Int,
                                   val relativeEndTime: Long,
                                   val pool: ApiMatchPool,
                                   val result: List<ApiMatchEntry>?) : ApiEventPayload(ApiEventPayloadType.ONE_TO_MANY_MATCH, eventVersion, createdAt) {

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
                domainPayload.endTime,
                ApiMatchPool(domainPayload.pool),
                domainPayload.result?.map { ApiMatchEntry(it) })
    }
}
