package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.RefusalEvent

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
        REFUSED_YOUNG,
        REFUSED_SICK,
        REFUSED_PREGNANT,
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
        RefusalEvent.Answer.REFUSED_YOUNG -> ApiRefusalEvent.ApiAnswer.REFUSED_YOUNG
        RefusalEvent.Answer.REFUSED_SICK -> ApiRefusalEvent.ApiAnswer.REFUSED_SICK
        RefusalEvent.Answer.REFUSED_PREGNANT -> ApiRefusalEvent.ApiAnswer.REFUSED_PREGNANT
        RefusalEvent.Answer.OTHER -> ApiRefusalEvent.ApiAnswer.OTHER
    }
