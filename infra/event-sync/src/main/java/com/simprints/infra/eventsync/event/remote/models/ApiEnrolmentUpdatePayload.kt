package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EnrolmentUpdateEvent.EnrolmentUpdatePayload
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiEnrolmentUpdatePayload(
    override val startTime: ApiTimestamp,
    val subjectId: String,
    val externalCredentialIdsToAdd: List<String>,
) : ApiEventPayload() {
    constructor(domainPayload: EnrolmentUpdatePayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.subjectId,
        domainPayload.externalCredentialIdsToAdd,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null
}
