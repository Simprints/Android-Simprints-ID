package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.util.*

@Keep
abstract class Event(
    val type: EventType,
    open val starTime: Long,
    open val endTime: Long? = null,
    val id: String = UUID.randomUUID().toString()) {

    var relativeStartTime: Long? = null
    var relativeEndTime: Long? = null

    fun updateRelativeTimes(sessionStartTime: Long) {
        if (relativeStartTime == null) {
            relativeStartTime = starTime - sessionStartTime
        }

        endTime?.let {
            if (relativeEndTime == null) {
                relativeEndTime = it - sessionStartTime
            }
        }
    }
}

