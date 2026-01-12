package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPool
import com.simprints.infra.eventsync.event.remote.models.ApiOneToManyMatchPayload.ApiOneToManyBatch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName(EventType.ONE_TO_MANY_MATCH_KEY)
internal data class ApiOneToManyMatchPayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val pool: ApiMatchPool,
    val matcher: String,
    val result: List<ApiMatchEntry>?,
    val probeBiometricReferenceId: String? = null,
    val batches: List<ApiOneToManyBatch>? = null,
) : ApiEventPayload() {
    @Keep
    @Serializable
    data class ApiMatchPool(
        val type: ApiMatchPoolType,
        val count: Int,
    ) {
        constructor(matchPool: MatchPool) :
            this(ApiMatchPoolType.valueOf(matchPool.type.toString()), matchPool.count)
    }

    @Keep
    @Serializable
    enum class ApiMatchPoolType {
        USER,
        MODULE,
        PROJECT,
    }

    @Keep
    @Serializable
    data class ApiOneToManyBatch(
        val loadingStartTime: ApiTimestamp,
        val loadingEndTime: ApiTimestamp,
        val comparingStartTime: ApiTimestamp,
        val comparingEndTime: ApiTimestamp,
        val count: Int,
    )

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
        batches = when (domainPayload) {
            is OneToManyMatchPayload.OneToManyMatchPayloadV2 -> null
            is OneToManyMatchPayload.OneToManyMatchPayloadV3 -> domainPayload.batches?.map { it.fromDomainToApi() }
        },
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}

private fun OneToManyMatchPayload.OneToManyBatch.fromDomainToApi() = ApiOneToManyBatch(
    loadingStartTime = loadingStartTime.fromDomainToApi(),
    loadingEndTime = loadingEndTime.fromDomainToApi(),
    comparingStartTime = comparingStartTime.fromDomainToApi(),
    comparingEndTime = comparingEndTime.fromDomainToApi(),
    count = count,
)
