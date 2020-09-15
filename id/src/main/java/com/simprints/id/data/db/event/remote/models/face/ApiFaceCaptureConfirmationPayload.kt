package com.simprints.id.data.db.event.remote.models.face

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.id.data.db.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload
import com.simprints.id.data.db.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload.Result.CONTINUE
import com.simprints.id.data.db.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload.Result.RECAPTURE
import com.simprints.id.data.db.event.remote.models.ApiEventPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType.FaceCaptureConfirmation
import com.simprints.id.data.db.event.remote.models.face.ApiFaceCaptureConfirmationPayload.ApiResult

@Keep
@JsonInclude(Include.NON_NULL)
data class ApiFaceCaptureConfirmationPayload(override val startTime: Long, //Not added on API yet
                                             val endTime: Long,
                                             override val version: Int,
                                             val result: ApiResult) : ApiEventPayload(FaceCaptureConfirmation, version, startTime) {

    constructor(domainPayload: FaceCaptureConfirmationPayload) : this(
        domainPayload.createdAt,
        domainPayload.endedAt,
        domainPayload.eventVersion,
        domainPayload.result.fromDomainToApi())

    enum class ApiResult {
        CONTINUE,
        RECAPTURE
    }
}


fun FaceCaptureConfirmationPayload.Result.fromDomainToApi() = when (this) {
    CONTINUE -> ApiResult.CONTINUE
    RECAPTURE -> ApiResult.RECAPTURE
}
