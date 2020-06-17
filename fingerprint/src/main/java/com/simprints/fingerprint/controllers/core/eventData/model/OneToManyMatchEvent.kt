package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import java.io.Serializable
import com.simprints.id.data.db.session.domain.models.events.OneToManyMatchEvent as CoreOneToManyMatchEvent
import com.simprints.id.data.db.session.domain.models.events.OneToManyMatchEvent.MatchPool as CoreMatchPool
import com.simprints.id.data.db.session.domain.models.events.OneToManyMatchEvent.MatchPoolType as CoreMatchPoolType

@Keep
class OneToManyMatchEvent(starTime: Long,
                          endTime: Long,
                          val query: Serializable,
                          val count: Int,
                          val result: List<MatchEntry>?) : Event(EventType.ONE_TO_MANY_MATCH, starTime, endTime)

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
