package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload
import com.simprints.infra.events.event.domain.models.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload.Result.CONTINUE
import com.simprints.infra.events.event.domain.models.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload.Result.RECAPTURE
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiFaceCaptureConfirmationPayload(
    override val startTime: ApiTimestamp, // Not added on API yet
    val endTime: ApiTimestamp?,
    val result: ApiResult,
) : ApiEventPayload() {
    constructor(domainPayload: FaceCaptureConfirmationPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.endedAt?.fromDomainToApi(),
        domainPayload.result.fromDomainToApi(),
    )

    enum class ApiResult {
        CONTINUE,
        RECAPTURE,
    }

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}

internal fun FaceCaptureConfirmationPayload.Result.fromDomainToApi() = when (this) {
    CONTINUE -> ApiFaceCaptureConfirmationPayload.ApiResult.CONTINUE
    RECAPTURE -> ApiFaceCaptureConfirmationPayload.ApiResult.RECAPTURE
}
