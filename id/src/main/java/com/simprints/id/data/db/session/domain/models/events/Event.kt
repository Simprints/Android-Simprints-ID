package com.simprints.id.data.db.session.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
abstract class Event(
    val type: EventType,
    open val startTime: Long,
    open val endTime: Long? = null,
    val id: String = UUID.randomUUID().toString()) {

    var relativeStartTime: Long? = null
    var relativeEndTime: Long? = null

    fun updateRelativeTimes(sessionStartTime: Long) {
        if (relativeStartTime == null) {
            relativeStartTime = startTime - sessionStartTime
        }

        endTime?.let {
            if (relativeEndTime == null) {
                relativeEndTime = it - sessionStartTime
            }
        }
    }
}

