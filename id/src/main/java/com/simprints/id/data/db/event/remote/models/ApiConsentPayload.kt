package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.ConsentEvent.ConsentPayload
import com.simprints.id.data.db.event.domain.models.ConsentEvent.ConsentPayload.Result.*
import com.simprints.id.data.db.event.domain.models.ConsentEvent.ConsentPayload.Type.INDIVIDUAL
import com.simprints.id.data.db.event.domain.models.ConsentEvent.ConsentPayload.Type.PARENTAL
import com.simprints.id.data.db.event.remote.models.ApiConsentPayload.ApiResult
import com.simprints.id.data.db.event.remote.models.ApiConsentPayload.ApiType

@Keep
class ApiConsentPayload(createdAt: Long,
                        eventVersion: Int,
                        var endedAt: Long,
                        val consentType: ApiType,
                        var result: ApiResult) : ApiEventPayload(ApiEventPayloadType.CONSENT, eventVersion, createdAt) {
    @Keep
    enum class ApiType {
        INDIVIDUAL, PARENTAL
    }

    @Keep
    enum class ApiResult {
        ACCEPTED, DECLINED, NO_RESPONSE
    }

    constructor(domainPayload: ConsentPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.endedAt,
            domainPayload.consentType.fromDomainToApi(),
            domainPayload.result.fromDomainToApi())
}

fun ConsentPayload.Type.fromDomainToApi() =
    when (this) {
        INDIVIDUAL -> ApiType.INDIVIDUAL
        PARENTAL -> ApiType.PARENTAL
    }

fun ConsentPayload.Result.fromDomainToApi() =
    when (this) {
        ACCEPTED -> ApiResult.ACCEPTED
        DECLINED -> ApiResult.DECLINED
        NO_RESPONSE -> ApiResult.NO_RESPONSE
    }
