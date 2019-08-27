package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.RefusalEvent as CoreRefusalEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.RefusalEvent.Answer as CoreRefusalAnswer
@Keep
class RefusalEvent(starTime: Long,
                   endTime: Long,
                   val reason: RefusalAnswer,
                   val otherText: String) : Event(EventType.REFUSAL, starTime, endTime)

@Keep
enum class RefusalAnswer {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    SCANNER_NOT_WORKING,
    REFUSED_NOT_PRESENT,
    REFUSED_YOUNG,
    OTHER
}

fun RefusalEvent.fromDomainToCore() =
    CoreRefusalEvent(starTime, endTime, reason.fromDomainToCore(), otherText)

fun RefusalAnswer.fromDomainToCore(): CoreRefusalAnswer =
    when(this) {
        RefusalAnswer.REFUSED_RELIGION -> CoreRefusalAnswer.REFUSED_RELIGION
        RefusalAnswer.REFUSED_DATA_CONCERNS -> CoreRefusalAnswer.REFUSED_DATA_CONCERNS
        RefusalAnswer.REFUSED_PERMISSION -> CoreRefusalAnswer.REFUSED_PERMISSION
        RefusalAnswer.SCANNER_NOT_WORKING -> CoreRefusalAnswer.SCANNER_NOT_WORKING
        RefusalAnswer.REFUSED_NOT_PRESENT -> CoreRefusalAnswer.REFUSED_NOT_PRESENT
        RefusalAnswer.REFUSED_YOUNG -> CoreRefusalAnswer.REFUSED_YOUNG
        RefusalAnswer.OTHER -> CoreRefusalAnswer.OTHER
    }
