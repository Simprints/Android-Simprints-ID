package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.FaceCaptureEvent

@Keep
class ApiFaceCaptureEvent(
    val relativeStartTime: Long,
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
        faceCaptureEvent.attemptNb,
        faceCaptureEvent.qualityThreshold,
        ApiResult.valueOf(faceCaptureEvent.result.name),
        faceCaptureEvent.isFallback,
        faceCaptureEvent.face?.let(::ApiFace)
    )

}
