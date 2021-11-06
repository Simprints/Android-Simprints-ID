package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.RefusalEvent as CoreRefusalEvent

@Keep
class RefusalEvent(
    startTime: Long,
    endTime: Long,
    val reason: RefusalAnswer,
    val otherText: String
) : Event(EventType.REFUSAL, startTime, endTime) {
    fun fromDomainToCore() =
        CoreRefusalEvent(startTime, endTime, reason.fromDomainToCore(), otherText)
}
