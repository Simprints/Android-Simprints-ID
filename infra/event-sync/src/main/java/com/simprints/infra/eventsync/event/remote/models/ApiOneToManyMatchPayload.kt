package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPool

@Keep
internal data class ApiOneToManyMatchPayload(
    override val startTime: ApiTimestamp,
    override val version: Int,
    val endTime: ApiTimestamp?,
    val pool: ApiMatchPool,
    val matcher: String,
    val result: List<ApiMatchEntry>?,
) : ApiEventPayload(version, startTime) {

    @Keep
    data class ApiMatchPool(val type: ApiMatchPoolType, val count: Int) {

        constructor(matchPool: MatchPool) :
            this(ApiMatchPoolType.valueOf(matchPool.type.toString()), matchPool.count)
    }

    @Keep
    enum class ApiMatchPoolType {

        USER,
        MODULE,
        PROJECT;
    }

    constructor(domainPayload: OneToManyMatchPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.eventVersion,
        domainPayload.endedAt?.fromDomainToApi(),
        ApiMatchPool(domainPayload.pool),
        domainPayload.matcher,
        domainPayload.result?.map { ApiMatchEntry(it) }
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? =
        null // this payload doesn't have tokenizable fields
}
