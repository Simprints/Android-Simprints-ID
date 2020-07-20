package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.RefusalEvent
import com.simprints.id.data.db.event.domain.models.RefusalEvent.RefusalPayload
import com.simprints.id.data.db.event.domain.models.RefusalEvent.RefusalPayload.Answer
import com.simprints.id.data.db.event.domain.models.RefusalEvent.RefusalPayload.Answer.*
import com.simprints.id.data.db.event.remote.events.ApiRefusalEvent.ApiRefusalPayload.ApiAnswer

@Keep
class ApiRefusalEvent(domainEvent: RefusalEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.map { it.fromDomainToApi() }.toMap(),
        domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiRefusalPayload(createdAt: Long,
                            eventVersion: Int,
                            val relativeEndTime: Long,
                            val reason: ApiAnswer,
                            val otherText: String) : ApiEventPayload(ApiEventPayloadType.REFUSAL, eventVersion, createdAt) {

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

        constructor(domainPayload: RefusalPayload) : this(
            domainPayload.createdAt,
            domainPayload.eventVersion,
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
