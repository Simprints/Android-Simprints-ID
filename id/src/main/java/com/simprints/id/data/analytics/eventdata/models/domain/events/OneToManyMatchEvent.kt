package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.domain.GROUP

class OneToManyMatchEvent(val relativeStartTime: Long,
                          val relativeEndTime: Long,
                          val pool: MatchPool,
                          val result: List<MatchEntry>?) : Event(EventType.ONE_TO_MANY_MATCH) {

    class MatchPool(val type: MatchPoolType, val count: Int)

    enum class MatchPoolType {
        USER,
        MODULE,
        PROJECT;

        companion object {
            fun fromConstantGroup(constantGroup: GROUP): MatchPoolType {
                return when (constantGroup) {
                    GROUP.GLOBAL -> PROJECT
                    GROUP.USER -> USER
                    GROUP.MODULE -> MODULE
                }
            }
        }
    }
}
