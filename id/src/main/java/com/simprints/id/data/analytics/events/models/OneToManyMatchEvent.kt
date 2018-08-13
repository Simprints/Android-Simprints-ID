package com.simprints.id.data.analytics.events.models

import com.simprints.id.domain.Constants

class OneToManyMatchEvent(val relativeStartTime: Long,
                          val relativeEndTime: Long,
                          val pool: MatchPool,
                          val result: MatchResult,
                          val matchResult: Array<MatchCandidate>?) : Event(EventType.ONE_TO_MANY_MATCH) {

    enum class MatchResult {
        SUCCESS,
        FAILURE
    }

    class MatchPool(val type: Constants.GROUP, val count: Int)
}
