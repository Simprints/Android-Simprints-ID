package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.FaceCaptureEvent

@Keep
class ApiFaceCaptureEvent(
    val relativeStartTime: Long,
    val relativeEndTime: Long,
    val attemptNb: Int,
    val qualityThreshold: Float,
    val result: ApiResult,
    val isFallback: Boolean,
    val face: ApiFace?
) : ApiEvent(ApiEventType.FACE_CAPTURE) {

    @Keep
    class ApiFace(
        val yaw: Float,
        var roll: Float,
        val quality: Float,
        val template: String
    ) {

        constructor(
            face: FaceCaptureEvent.Face
        ) : this(face.yaw, face.roll, face.quality, face.template)

    }

    @Keep
    enum class ApiResult {
        VALID,
        INVALID,
        OFF_YAW,
        OFF_ROLL,
        TOO_CLOSE,
        TOO_FAR
    }

    constructor(faceCaptureEvent: FaceCaptureEvent) : this(
        faceCaptureEvent.relativeStartTime ?: 0,
        faceCaptureEvent.relativeEndTime ?: 0,
        faceCaptureEvent.attemptNb,
        faceCaptureEvent.qualityThreshold,
        faceCaptureEvent.result.fromDomainToApi(),
        faceCaptureEvent.isFallback,
        faceCaptureEvent.face?.let(::ApiFace)
    )

}

fun FaceCaptureEvent.Result.fromDomainToApi() = when (this) {
    FaceCaptureEvent.Result.VALID -> ApiFaceCaptureEvent.ApiResult.VALID
    FaceCaptureEvent.Result.INVALID -> ApiFaceCaptureEvent.ApiResult.INVALID
    FaceCaptureEvent.Result.OFF_YAW -> ApiFaceCaptureEvent.ApiResult.OFF_YAW
    FaceCaptureEvent.Result.OFF_ROLL -> ApiFaceCaptureEvent.ApiResult.OFF_ROLL
    FaceCaptureEvent.Result.TOO_CLOSE -> ApiFaceCaptureEvent.ApiResult.TOO_CLOSE
    FaceCaptureEvent.Result.TOO_FAR -> ApiFaceCaptureEvent.ApiResult.TOO_FAR
}
