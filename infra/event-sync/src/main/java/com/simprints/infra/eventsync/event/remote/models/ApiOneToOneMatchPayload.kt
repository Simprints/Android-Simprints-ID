package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.OneToOneMatchEvent.OneToOneMatchPayload
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiOneToOneMatchPayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val candidateId: String,
    val matcher: String,
    val result: ApiMatchEntry?,
    val fingerComparisonStrategy: ApiFingerComparisonStrategy?,
    val probeBiometricReferenceId: String? = null,
) : ApiEventPayload() {
    constructor(domainPayload: OneToOneMatchPayload) : this(
        startTime = domainPayload.createdAt.fromDomainToApi(),
        endTime = domainPayload.endedAt?.fromDomainToApi(),
        candidateId = domainPayload.candidateId,
        matcher = domainPayload.matcher,
        result = domainPayload.result?.let { ApiMatchEntry(it) },
        fingerComparisonStrategy = domainPayload.fingerComparisonStrategy?.fromDomainToApi(),
        when (domainPayload) {
            is OneToOneMatchPayload.OneToOneMatchPayloadV3 -> null
            is OneToOneMatchPayload.OneToOneMatchPayloadV4 -> domainPayload.probeBiometricReferenceId
        },
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}
