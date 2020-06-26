package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.FaceCaptureConfirmationEvent

@Keep
class ApiFaceCaptureConfirmationEvent(
    val relativeStartTime: Long,
    val result: ApiResult
) : ApiEvent(ApiEventType.FACE_CAPTURE_CONFIRMATION) {

    @Keep
    enum class ApiResult {
        CONTINUE,
        RECAPTURE
    }

    constructor(faceCaptureConfirmationEvent: FaceCaptureConfirmationEvent) : this(
        faceCaptureConfirmationEvent.relativeStartTime ?: 0,
        faceCaptureConfirmationEvent.result.fromDomainToApi()
    )

}

fun FaceCaptureConfirmationEvent.Result.fromDomainToApi() = when (this) {
    FaceCaptureConfirmationEvent.Result.CONTINUE -> ApiFaceCaptureConfirmationEvent.ApiResult.CONTINUE
    FaceCaptureConfirmationEvent.Result.RECAPTURE -> ApiFaceCaptureConfirmationEvent.ApiResult.RECAPTURE
}
