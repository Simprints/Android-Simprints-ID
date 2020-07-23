package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep
import com.simprints.moduleapi.face.responses.IFaceExitReason
import com.simprints.id.data.db.event.domain.models.RefusalEvent as CoreRefusalEvent
import com.simprints.id.data.db.event.domain.models.RefusalEvent.RefusalPayload.Answer as CoreRefusalAnswer

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
