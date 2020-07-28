package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.OneToManyMatchEvent

@Keep
class ApiOneToManyMatchEvent(
    val relativeStartTime: Long,
    val relativeEndTime: Long,
    val pool: ApiMatchPool,
    val matcher: ApiMatcher,
    val result: List<ApiMatchEntry>?
) : ApiEvent(ApiEventType.ONE_TO_MANY_MATCH) {

    @Keep
    class ApiMatchPool(val type: ApiMatchPoolType, val count: Int) {
        constructor(matchPool: OneToManyMatchEvent.MatchPool) :
            this(matchPool.type.fromDomainToApi(), matchPool.count)
    }

    @Keep
    enum class ApiMatchPoolType {
        USER,
        MODULE,
        PROJECT
    }

    constructor(oneToManyMatchEvent: OneToManyMatchEvent) :
        this(oneToManyMatchEvent.relativeStartTime ?: 0,
            oneToManyMatchEvent.relativeEndTime ?: 0,
            ApiMatchPool(oneToManyMatchEvent.pool),
            oneToManyMatchEvent.matcher.fromDomainToApi(),
            oneToManyMatchEvent.result?.map { ApiMatchEntry(it) })
}

fun OneToManyMatchEvent.MatchPoolType.fromDomainToApi() = when (this) {
    OneToManyMatchEvent.MatchPoolType.USER -> ApiOneToManyMatchEvent.ApiMatchPoolType.USER
    OneToManyMatchEvent.MatchPoolType.MODULE -> ApiOneToManyMatchEvent.ApiMatchPoolType.MODULE
    OneToManyMatchEvent.MatchPoolType.PROJECT -> ApiOneToManyMatchEvent.ApiMatchPoolType.PROJECT
}
