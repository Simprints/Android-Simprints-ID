package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.domain.GROUP

@Keep
class OneToManyMatchEvent(val relativeStartTime: Long,
                          val relativeEndTime: Long,
                          val pool: MatchPool,
                          val result: List<MatchEntry>?) : Event(EventType.ONE_TO_MANY_MATCH) {

    @Keep
    class MatchPool(val type: MatchPoolType, val count: Int)

    @Keep
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
