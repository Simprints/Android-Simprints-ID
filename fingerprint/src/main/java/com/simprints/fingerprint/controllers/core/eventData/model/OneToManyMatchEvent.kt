package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.db.subject.local.SubjectQuery
import java.io.Serializable
import com.simprints.id.data.db.event.domain.models.OneToManyMatchEvent as CoreOneToManyMatchEvent
import com.simprints.id.data.db.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPool as CoreMatchPool
import com.simprints.id.data.db.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType as CoreMatchPoolType

@Keep
class OneToManyMatchEvent(
    startTime: Long,
    endTime: Long,
    val query: Serializable,
    val count: Int,
    val matcher: Matcher,
    val result: List<MatchEntry>?
) : Event(EventType.ONE_TO_MANY_MATCH, startTime, endTime)

fun OneToManyMatchEvent.fromDomainToCore() =
    CoreOneToManyMatchEvent(
        startTime,
        endTime,
        (query as SubjectQuery).asCoreMatchPool(count),
        matcher.fromDomainToCore(),
        result?.map { it.fromDomainToCore() }
    )

fun SubjectQuery.asCoreMatchPool(count: Int) =
    CoreMatchPool(this.parseQueryAsCoreMatchPoolType(), count)

fun SubjectQuery.parseQueryAsCoreMatchPoolType(): CoreMatchPoolType =
    when {
        this.attendantId != null -> CoreMatchPoolType.USER
        this.moduleId != null -> CoreMatchPoolType.MODULE
        else -> CoreMatchPoolType.PROJECT
    }
