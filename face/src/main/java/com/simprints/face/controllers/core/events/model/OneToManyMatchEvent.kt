package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import java.io.Serializable
import com.simprints.id.data.db.session.domain.models.events.Matcher as CoreMatcher
import com.simprints.id.data.db.session.domain.models.events.OneToManyMatchEvent as CoreOneToManyMatchEvent
import com.simprints.id.data.db.session.domain.models.events.OneToManyMatchEvent.MatchPool as CoreMatchPool
import com.simprints.id.data.db.session.domain.models.events.OneToManyMatchEvent.MatchPoolType as CoreMatchPoolType

@Keep
class OneToManyMatchEvent(
    startTime: Long,
    endTime: Long,
    val query: Serializable,
    val count: Int,
    val matcher: Matcher,
    val result: List<MatchEntry>?
) : Event(EventType.ONE_TO_MANY_MATCH, startTime, endTime) {

    // TODO: add matcher as a parameter of Core Match Event
    fun fromDomainToCore() = CoreOneToManyMatchEvent(
        startTime,
        endTime,
        (query as SubjectLocalDataSource.Query).asCoreMatchPool(count),
        CoreMatcher.RANK_ONE, // TODO: implement Matcher in face module
        result?.map { it.fromDomainToCore() }
    )

    private fun SubjectLocalDataSource.Query.asCoreMatchPool(count: Int) =
        CoreMatchPool(this.parseQueryAsCoreMatchPoolType(), count)

    private fun SubjectLocalDataSource.Query.parseQueryAsCoreMatchPoolType(): CoreMatchPoolType =
        when {
            this.attendantId != null -> CoreMatchPoolType.USER
            this.moduleId != null -> CoreMatchPoolType.MODULE
            else -> CoreMatchPoolType.PROJECT
        }
}
