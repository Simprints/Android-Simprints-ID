package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import java.io.Serializable
import com.simprints.eventsystem.event.domain.models.OneToManyMatchEvent as CoreOneToManyMatchEvent
import com.simprints.eventsystem.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPool as CoreMatchPool
import com.simprints.eventsystem.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType as CoreMatchPoolType

/**
 * This class represents a fingerprint identification event (i.e. one-to-many match event).
 *
 * @property query  the query for extracting the matching candidates, see [SubjectQuery]
 * @property count  the number of matching candidates pulled from the query
 * @property matcher  the matching algorithm used in filtering the candidates
 * @property result  the filtered list of top matching candidates
 *
 */
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
