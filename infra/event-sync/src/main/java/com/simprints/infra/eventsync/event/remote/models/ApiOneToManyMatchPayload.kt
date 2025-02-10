package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPool

@Keep
internal data class ApiOneToManyMatchPayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val pool: ApiMatchPool,
    val matcher: String,
    val result: List<ApiMatchEntry>?,
    val probeBiometricReferenceId: String? = null,
) : ApiEventPayload(startTime) {
    @Keep
    data class ApiMatchPool(
        val type: ApiMatchPoolType,
        val count: Int,
    ) {
        constructor(matchPool: MatchPool) :
            this(ApiMatchPoolType.valueOf(matchPool.type.toString()), matchPool.count)
    }

    @Keep
    enum class ApiMatchPoolType {
        USER,
        MODULE,
        PROJECT,
    }

    constructor(domainPayload: OneToManyMatchPayload) : this(
        startTime = domainPayload.createdAt.fromDomainToApi(),
        endTime = domainPayload.endedAt?.fromDomainToApi(),
        pool = ApiMatchPool(domainPayload.pool),
        matcher = domainPayload.matcher,
        result = domainPayload.result?.map { ApiMatchEntry(it) },
        probeBiometricReferenceId = when (domainPayload) {
            is OneToManyMatchPayload.OneToManyMatchPayloadV2 -> null
            is OneToManyMatchPayload.OneToManyMatchPayloadV3 -> domainPayload.probeBiometricReferenceId
        },
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}
