package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.RefusalEvent
import com.simprints.id.data.db.event.domain.events.RefusalEvent.RefusalPayload
import com.simprints.id.data.db.event.domain.events.RefusalEvent.RefusalPayload.Answer
import com.simprints.id.data.db.event.domain.events.RefusalEvent.RefusalPayload.Answer.*
import com.simprints.id.data.db.event.remote.events.ApiRefusalEvent.ApiRefusalPayload.ApiAnswer

@Keep
class ApiRefusalEvent(domainEvent: RefusalEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.fromDomainToApi(),
        domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiRefusalPayload(val relativeStartTime: Long,
                            val relativeEndTime: Long,
                            val reason: ApiAnswer,
                            val otherText: String) : ApiEventPayload(ApiEventPayloadType.REFUSAL) {

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

        constructor(domainPayload: RefusalPayload) :
            this(domainPayload.creationTime,
                domainPayload.endTime,
                domainPayload.reason.toApiRefusalEventAnswer(),
                domainPayload.otherText)
    }
}

fun Answer.toApiRefusalEventAnswer() =
    when (this) {
        REFUSED_RELIGION -> ApiAnswer.REFUSED_RELIGION
        REFUSED_DATA_CONCERNS -> ApiAnswer.REFUSED_DATA_CONCERNS
        REFUSED_PERMISSION -> ApiAnswer.REFUSED_PERMISSION
        SCANNER_NOT_WORKING -> ApiAnswer.SCANNER_NOT_WORKING
        REFUSED_NOT_PRESENT -> ApiAnswer.REFUSED_NOT_PRESENT
        REFUSED_YOUNG -> ApiAnswer.REFUSED_YOUNG
        OTHER -> ApiAnswer.OTHER
    }
