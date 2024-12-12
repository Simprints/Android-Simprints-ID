package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.RefusalEvent.RefusalPayload
import com.simprints.infra.events.event.domain.models.RefusalEvent.RefusalPayload.Answer.APP_NOT_WORKING
import com.simprints.infra.events.event.domain.models.RefusalEvent.RefusalPayload.Answer.OTHER
import com.simprints.infra.events.event.domain.models.RefusalEvent.RefusalPayload.Answer.REFUSED_DATA_CONCERNS
import com.simprints.infra.events.event.domain.models.RefusalEvent.RefusalPayload.Answer.REFUSED_NOT_PRESENT
import com.simprints.infra.events.event.domain.models.RefusalEvent.RefusalPayload.Answer.REFUSED_PERMISSION
import com.simprints.infra.events.event.domain.models.RefusalEvent.RefusalPayload.Answer.REFUSED_RELIGION
import com.simprints.infra.events.event.domain.models.RefusalEvent.RefusalPayload.Answer.REFUSED_YOUNG
import com.simprints.infra.events.event.domain.models.RefusalEvent.RefusalPayload.Answer.SCANNER_NOT_WORKING
import com.simprints.infra.events.event.domain.models.RefusalEvent.RefusalPayload.Answer.UNCOOPERATIVE_CHILD
import com.simprints.infra.events.event.domain.models.RefusalEvent.RefusalPayload.Answer.WRONG_AGE_GROUP_SELECTED
import com.simprints.infra.eventsync.event.remote.models.ApiRefusalPayload.ApiAnswer

@Keep
internal data class ApiRefusalPayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val reason: ApiAnswer,
    val otherText: String,
) : ApiEventPayload(startTime) {
    @Keep
    enum class ApiAnswer {
        REFUSED_RELIGION,
        REFUSED_DATA_CONCERNS,
        REFUSED_PERMISSION,
        SCANNER_NOT_WORKING,
        APP_NOT_WORKING,
        REFUSED_NOT_PRESENT,
        REFUSED_YOUNG,
        WRONG_AGE_GROUP_SELECTED,
        UNCOOPERATIVE_CHILD,
        OTHER,
    }

    constructor(domainPayload: RefusalPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.endedAt?.fromDomainToApi(),
        domainPayload.reason.toApiRefusalEventAnswer(),
        domainPayload.otherText,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}

internal fun RefusalPayload.Answer.toApiRefusalEventAnswer() = when (this) {
    REFUSED_RELIGION -> ApiAnswer.REFUSED_RELIGION
    REFUSED_DATA_CONCERNS -> ApiAnswer.REFUSED_DATA_CONCERNS
    REFUSED_PERMISSION -> ApiAnswer.REFUSED_PERMISSION
    SCANNER_NOT_WORKING -> ApiAnswer.SCANNER_NOT_WORKING
    APP_NOT_WORKING -> ApiAnswer.APP_NOT_WORKING
    REFUSED_NOT_PRESENT -> ApiAnswer.REFUSED_NOT_PRESENT
    REFUSED_YOUNG -> ApiAnswer.REFUSED_YOUNG
    WRONG_AGE_GROUP_SELECTED -> ApiAnswer.WRONG_AGE_GROUP_SELECTED
    UNCOOPERATIVE_CHILD -> ApiAnswer.UNCOOPERATIVE_CHILD
    OTHER -> ApiAnswer.OTHER
}
