package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.fingerprint.controllers.core.eventData.model.OneToManyMatchEvent.MatchPool
import com.simprints.fingerprint.controllers.core.preferencesManager.MatchPoolType
import com.simprints.id.data.analytics.eventdata.models.domain.events.OneToManyMatchEvent.MatchPoolType as CoreMatchPoolType
import com.simprints.id.data.analytics.eventdata.models.domain.events.OneToManyMatchEvent.MatchPool as CoreMatchPool

@Keep
class OneToManyMatchEvent(starTime: Long,
                          endTime: Long,
                          val pool: MatchPool,
                          val result: List<MatchEntry>?) : Event(EventType.ONE_TO_MANY_MATCH, starTime, endTime) {

    @Keep
    class MatchPool(val type: MatchPoolType, val count: Int)
}

fun MatchPool.fromDomainToCore() =
    CoreMatchPool(type.fromDomainToCore(), count)

fun MatchPoolType.fromDomainToCore(): CoreMatchPoolType =
    when(this) {
        MatchPoolType.USER -> CoreMatchPoolType.USER
        MatchPoolType.MODULE -> CoreMatchPoolType.MODULE
        MatchPoolType.PROJECT -> CoreMatchPoolType.PROJECT
    }
