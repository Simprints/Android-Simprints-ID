package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EnrolmentUpdateEvent.EnrolmentUpdatePayload

@Keep
internal data class ApiEnrolmentUpdatePayload(
    override val startTime: ApiTimestamp,
    val subjectId: String,
    val externalCredentialIdsToAdd: List<String>,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: EnrolmentUpdatePayload) : this(
        domainPayload.startTime.fromDomainToApi(),
        domainPayload.subjectId,
        domainPayload.externalCredentialIdsToAdd,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null
}
