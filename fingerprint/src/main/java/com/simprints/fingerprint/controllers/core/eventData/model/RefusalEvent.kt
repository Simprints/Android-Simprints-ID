package com.simprints.fingerprint.controllers.core.eventData.model;

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
    BENEFICIARY_REFUSED,
    SCANNER_NOT_WORKING,
    OTHER;
}

fun RefusalEvent.fromDomainToCore() =
    CoreRefusalEvent(starTime, endTime, reason.fromDomainToCore(), otherText)

fun RefusalAnswer.fromDomainToCore(): CoreRefusalAnswer =
    when(this) {
        RefusalAnswer.BENEFICIARY_REFUSED -> CoreRefusalAnswer.BENEFICIARY_REFUSED
        RefusalAnswer.SCANNER_NOT_WORKING -> CoreRefusalAnswer.SCANNER_NOT_WORKING
        RefusalAnswer.OTHER -> CoreRefusalAnswer.OTHER
    }
