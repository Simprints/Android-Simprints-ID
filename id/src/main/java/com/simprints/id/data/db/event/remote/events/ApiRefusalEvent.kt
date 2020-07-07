package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.RefusalEvent
import com.simprints.id.data.db.event.domain.events.RefusalEvent.RefusalPayload
import com.simprints.id.data.db.event.domain.events.RefusalEvent.RefusalPayload.Answer

@Keep
class ApiRefusalEvent(val relativeStartTime: Long,
                      val relativeEndTime: Long,
                      val reason: ApiAnswer,
                      val otherText: String): ApiEvent(ApiEventType.REFUSAL) {

    @Keep
    enum class ApiAnswer {
        REFUSED_RELIGION,
        REFUSED_DATA_CONCERNS,
        REFUSED_PERMISSION,
        SCANNER_NOT_WORKING,
        REFUSED_NOT_PRESENT,
        REFUSED_YOUNG,
        OTHER
    }

    constructor(refusalEvent: RefusalEvent) :
        this((refusalEvent.payload as RefusalPayload).creationTime,
            refusalEvent.payload.endTime,
            refusalEvent.payload.reason.toApiRefusalEventAnswer(),
            refusalEvent.payload.otherText)
}

fun Answer.toApiRefusalEventAnswer() =
    when(this) {
        Answer.REFUSED_RELIGION -> ApiRefusalEvent.ApiAnswer.REFUSED_RELIGION
        Answer.REFUSED_DATA_CONCERNS -> ApiRefusalEvent.ApiAnswer.REFUSED_DATA_CONCERNS
        Answer.REFUSED_PERMISSION -> ApiRefusalEvent.ApiAnswer.REFUSED_PERMISSION
        Answer.SCANNER_NOT_WORKING -> ApiRefusalEvent.ApiAnswer.SCANNER_NOT_WORKING
        Answer.REFUSED_NOT_PRESENT -> ApiRefusalEvent.ApiAnswer.REFUSED_NOT_PRESENT
        Answer.REFUSED_YOUNG -> ApiRefusalEvent.ApiAnswer.REFUSED_YOUNG
        Answer.OTHER -> ApiRefusalEvent.ApiAnswer.OTHER
    }
