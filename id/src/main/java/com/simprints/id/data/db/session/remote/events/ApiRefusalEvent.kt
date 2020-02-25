package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.events.RefusalEvent

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
        this(refusalEvent.relativeStartTime ?: 0,
            refusalEvent.relativeEndTime ?: 0,
            refusalEvent.reason.toApiRefusalEventAnswer(),
            refusalEvent.otherText)
}

fun RefusalEvent.Answer.toApiRefusalEventAnswer() =
    when(this) {
        RefusalEvent.Answer.REFUSED_RELIGION -> ApiRefusalEvent.ApiAnswer.REFUSED_RELIGION
        RefusalEvent.Answer.REFUSED_DATA_CONCERNS -> ApiRefusalEvent.ApiAnswer.REFUSED_DATA_CONCERNS
        RefusalEvent.Answer.REFUSED_PERMISSION -> ApiRefusalEvent.ApiAnswer.REFUSED_PERMISSION
        RefusalEvent.Answer.SCANNER_NOT_WORKING -> ApiRefusalEvent.ApiAnswer.SCANNER_NOT_WORKING
        RefusalEvent.Answer.REFUSED_NOT_PRESENT -> ApiRefusalEvent.ApiAnswer.REFUSED_NOT_PRESENT
        RefusalEvent.Answer.REFUSED_YOUNG -> ApiRefusalEvent.ApiAnswer.REFUSED_YOUNG
        RefusalEvent.Answer.OTHER -> ApiRefusalEvent.ApiAnswer.OTHER
    }
