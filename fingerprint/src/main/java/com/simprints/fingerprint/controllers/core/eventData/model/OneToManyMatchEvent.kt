package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import java.io.Serializable
import com.simprints.id.data.analytics.eventdata.models.domain.events.OneToManyMatchEvent as CoreOneToManyMatchEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.OneToManyMatchEvent.MatchPool as CoreMatchPool
import com.simprints.id.data.analytics.eventdata.models.domain.events.OneToManyMatchEvent.MatchPoolType as CoreMatchPoolType

@Keep
class OneToManyMatchEvent(starTime: Long,
                          endTime: Long,
                          val query: Serializable,
                          val count: Int,
                          val result: List<MatchEntry>?) : Event(EventType.ONE_TO_MANY_MATCH, starTime, endTime)

fun OneToManyMatchEvent.fromDomainToCore() =
    CoreOneToManyMatchEvent(
        starTime,
        endTime,
        (query as PersonLocalDataSource.Query).asCoreMatchPool(count),
        result?.map { it.fromDomainToCore() }
    )

fun PersonLocalDataSource.Query.asCoreMatchPool(count: Int) =
    CoreMatchPool(this.parseQueryAsCoreMatchPoolType(), count)

fun PersonLocalDataSource.Query.parseQueryAsCoreMatchPoolType(): CoreMatchPoolType =
    when {
        this.userId != null -> CoreMatchPoolType.USER
        this.moduleId != null -> CoreMatchPoolType.MODULE
        else -> CoreMatchPoolType.PROJECT
    }
