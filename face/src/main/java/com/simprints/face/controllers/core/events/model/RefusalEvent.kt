package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep
import com.simprints.moduleapi.face.responses.IFaceExitReason
import com.simprints.id.data.db.event.domain.events.RefusalEvent as CoreRefusalEvent
import com.simprints.id.data.db.event.domain.events.RefusalEvent.RefusalPayload.Answer as CoreRefusalAnswer

@Keep
class RefusalEvent(startTime: Long,
                   endTime: Long,
                   val reason: RefusalAnswer,
                   val otherText: String) : Event(EventType.REFUSAL, startTime, endTime) {
    fun fromDomainToCore() = CoreRefusalEvent(startTime, endTime, reason.fromDomainToCore(), otherText)
}

@Keep
enum class RefusalAnswer {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    APP_NOT_WORKING,
    REFUSED_NOT_PRESENT,
    REFUSED_YOUNG,
    OTHER;

    fun fromDomainToCore(): CoreRefusalAnswer =
        when (this) {
            REFUSED_RELIGION -> CoreRefusalAnswer.REFUSED_RELIGION
            REFUSED_DATA_CONCERNS -> CoreRefusalAnswer.REFUSED_DATA_CONCERNS
            REFUSED_PERMISSION -> CoreRefusalAnswer.REFUSED_PERMISSION
            APP_NOT_WORKING -> CoreRefusalAnswer.OTHER // TODO: Map to correct Hawkeye APP_NOT_WORKING when ready
            REFUSED_NOT_PRESENT -> CoreRefusalAnswer.REFUSED_NOT_PRESENT
            REFUSED_YOUNG -> CoreRefusalAnswer.REFUSED_YOUNG
            OTHER -> CoreRefusalAnswer.OTHER
        }

    fun fromDomainToExitReason(): IFaceExitReason =
        when (this) {
            REFUSED_RELIGION -> IFaceExitReason.REFUSED_RELIGION
            REFUSED_DATA_CONCERNS -> IFaceExitReason.REFUSED_DATA_CONCERNS
            REFUSED_PERMISSION -> IFaceExitReason.REFUSED_PERMISSION
            APP_NOT_WORKING -> IFaceExitReason.APP_NOT_WORKING
            REFUSED_NOT_PRESENT -> IFaceExitReason.REFUSED_NOT_PRESENT
            REFUSED_YOUNG -> IFaceExitReason.REFUSED_YOUNG
            OTHER -> IFaceExitReason.OTHER
        }

}


