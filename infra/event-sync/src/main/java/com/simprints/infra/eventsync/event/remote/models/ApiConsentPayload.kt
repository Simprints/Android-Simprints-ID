package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.ConsentEvent.ConsentPayload
import com.simprints.infra.events.event.domain.models.ConsentEvent.ConsentPayload.Result.ACCEPTED
import com.simprints.infra.events.event.domain.models.ConsentEvent.ConsentPayload.Result.DECLINED
import com.simprints.infra.events.event.domain.models.ConsentEvent.ConsentPayload.Result.NO_RESPONSE
import com.simprints.infra.events.event.domain.models.ConsentEvent.ConsentPayload.Type.INDIVIDUAL
import com.simprints.infra.events.event.domain.models.ConsentEvent.ConsentPayload.Type.PARENTAL
import com.simprints.infra.eventsync.event.remote.models.ApiConsentPayload.ApiResult
import com.simprints.infra.eventsync.event.remote.models.ApiConsentPayload.ApiType

@Keep
internal data class ApiConsentPayload(
    override val startTime: ApiTimestamp,
    var endTime: ApiTimestamp?,
    val consentType: ApiType,
    var result: ApiResult,
) : ApiEventPayload(startTime) {
    @Keep
    enum class ApiType {
        INDIVIDUAL,
        PARENTAL,
    }

    @Keep
    enum class ApiResult {
        ACCEPTED,
        DECLINED,
        NO_RESPONSE,
    }

    constructor(domainPayload: ConsentPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.endedAt?.fromDomainToApi(),
        domainPayload.consentType.fromDomainToApi(),
        domainPayload.result.fromDomainToApi(),
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}

internal fun ConsentPayload.Type.fromDomainToApi() = when (this) {
    INDIVIDUAL -> ApiType.INDIVIDUAL
    PARENTAL -> ApiType.PARENTAL
}

internal fun ConsentPayload.Result.fromDomainToApi() = when (this) {
    ACCEPTED -> ApiResult.ACCEPTED
    DECLINED -> ApiResult.DECLINED
    NO_RESPONSE -> ApiResult.NO_RESPONSE
}
