package com.simprints.id.data.analytics.events.models

import com.simprints.id.domain.Constants

class OneToManyMatchEvent(val relativeStartTime: Long,
                          val relativeEndTime: Long,
                          val pool: MatchPool,
                          val matchResult: Array<MatchCandidate>?) : Event(EventType.ONE_TO_MANY_MATCH) {

    class MatchPool(val type: MatchPoolType, val count: Int)

    enum class MatchPoolType {
        USER,
        MODULE,
        PROJECT;

        companion object {
            fun fromConstantGroup(constantGroup: Constants.GROUP): MatchPoolType {
                return when (constantGroup) {
                    Constants.GROUP.GLOBAL -> MatchPoolType.PROJECT
                    Constants.GROUP.USER -> MatchPoolType.USER
                    Constants.GROUP.MODULE -> MatchPoolType.MODULE
                }
            }
        }
    }
}
