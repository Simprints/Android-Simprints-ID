package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep
import com.simprints.core.tools.utils.randomUUID

@Keep
abstract class Event(
    val type: EventType,
    open val startTime: Long,
    open val endTime: Long? = null,
    open val id: String = randomUUID()
) {

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

