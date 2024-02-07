package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EnrolmentEventV1

@Keep
internal data class ApiEnrolmentPayloadV1(
    override val startTime: ApiTimestamp,
    override val version: Int,
    val personId: String,
) : ApiEventPayload(version, startTime) {

    constructor(domainPayload: EnrolmentEventV1.EnrolmentPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.eventVersion,
        domainPayload.personId
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? =
        null // this payload doesn't have tokenizable fields
}
