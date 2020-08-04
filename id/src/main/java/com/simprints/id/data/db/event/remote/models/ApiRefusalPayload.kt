package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.RefusalEvent.RefusalPayload
import com.simprints.id.data.db.event.domain.models.RefusalEvent.RefusalPayload.Answer.*
import com.simprints.id.data.db.event.remote.models.ApiRefusalPayload.ApiAnswer

@Keep
data class ApiRefusalPayload(override val relativeStartTime: Long,
                             override val version: Int,
                             val relativeEndTime: Long,
                             val reason: ApiAnswer,
                             val otherText: String) : ApiEventPayload(ApiEventPayloadType.Refusal, version, relativeStartTime) {

    @Keep
    enum class ApiAnswer {
        REFUSED_RELIGION,
        REFUSED_DATA_CONCERNS,
        REFUSED_PERMISSION,
        SCANNER_NOT_WORKING,
        APP_NOT_WORKING,
        REFUSED_NOT_PRESENT,
        REFUSED_YOUNG,
        OTHER
    }

    constructor(domainPayload: RefusalPayload) : this(
        domainPayload.createdAt,
        domainPayload.eventVersion,
        domainPayload.endedAt,
        domainPayload.reason.toApiRefusalEventAnswer(),
        domainPayload.otherText)
}


fun RefusalPayload.Answer.toApiRefusalEventAnswer() =
    when (this) {
        REFUSED_RELIGION -> ApiAnswer.REFUSED_RELIGION
        REFUSED_DATA_CONCERNS -> ApiAnswer.REFUSED_DATA_CONCERNS
        REFUSED_PERMISSION -> ApiAnswer.REFUSED_PERMISSION
        SCANNER_NOT_WORKING -> ApiAnswer.SCANNER_NOT_WORKING
        APP_NOT_WORKING -> ApiAnswer.APP_NOT_WORKING
        REFUSED_NOT_PRESENT -> ApiAnswer.REFUSED_NOT_PRESENT
        REFUSED_YOUNG -> ApiAnswer.REFUSED_YOUNG
        OTHER -> ApiAnswer.OTHER
    }
