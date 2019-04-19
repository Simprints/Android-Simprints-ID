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
        BENEFICIARY_REFUSED,
        SCANNER_NOT_WORKING,
        OTHER
    }

    constructor(refusalEvent: RefusalEvent) :
        this(refusalEvent.relativeStartTime,
            refusalEvent.relativeEndTime,
            ApiAnswer.valueOf(refusalEvent.reason.toString()),
            refusalEvent.otherText)
}
