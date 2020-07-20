package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
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
    val result: List<MatchEntry>?
) : Event(EventType.ONE_TO_MANY_MATCH, startTime, endTime)

fun OneToManyMatchEvent.fromDomainToCore() =
    CoreOneToManyMatchEvent(
        startTime,
        endTime,
        (query as SubjectLocalDataSource.Query).asCoreMatchPool(count),
        result?.map { it.fromDomainToCore() }
    )

fun SubjectLocalDataSource.Query.asCoreMatchPool(count: Int) =
    CoreMatchPool(this.parseQueryAsCoreMatchPoolType(), count)

fun SubjectLocalDataSource.Query.parseQueryAsCoreMatchPoolType(): CoreMatchPoolType =
    when {
        this.attendantId != null -> CoreMatchPoolType.USER
        this.moduleId != null -> CoreMatchPoolType.MODULE
        else -> CoreMatchPoolType.PROJECT
    }
