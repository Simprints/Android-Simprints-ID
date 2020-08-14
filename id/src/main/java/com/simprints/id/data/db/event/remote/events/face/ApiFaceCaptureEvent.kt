package com.simprints.id.data.db.event.remote.events.face

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.face.FaceCaptureEvent
import com.simprints.id.data.db.event.domain.models.face.FaceCaptureEvent.FaceCapturePayload
import com.simprints.id.data.db.event.remote.events.ApiEvent
import com.simprints.id.data.db.event.remote.events.ApiEventPayload
import com.simprints.id.data.db.event.remote.events.ApiEventPayloadType.FACE_CAPTURE
import com.simprints.id.data.db.event.remote.events.face.ApiFaceCaptureEvent.ApiFace
import com.simprints.id.data.db.event.remote.events.face.ApiFaceCaptureEvent.ApiResult.*
import com.simprints.id.data.db.event.remote.events.fromDomainToApi

@Keep
class ApiFaceCaptureEvent(
    val domainEvent: FaceCaptureEvent
) : ApiEvent(
    domainEvent.id,
    domainEvent.labels.fromDomainToApi(),
    domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiFaceCapturePayload(val id: String,
                                createdAt: Long,
                                eventVersion: Int,
                                val attemptNb: Int,
                                val qualityThreshold: Float,
                                val result: ApiResult,
                                val isFallback: Boolean,
                                val face: ApiFace?) : ApiEventPayload(FACE_CAPTURE, eventVersion, createdAt) {

        constructor(domainPayload: FaceCapturePayload) : this(
            "id", //STOPSHIP
            domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.attemptNb,
            domainPayload.qualityThreshold,
            domainPayload.result.fromDomainToApi(),
            domainPayload.isFallback,
            domainPayload.face?.fromDomainToApi())
    }

    @Keep
    data class ApiFace(
        val yaw: Float,
        var roll: Float,
        val quality: Float,
        val template: String
    )

    @Keep
    enum class ApiResult {
        VALID,
        INVALID,
        OFF_YAW,
        OFF_ROLL,
        TOO_CLOSE,
        TOO_FAR
    }
}

fun FaceCapturePayload.Face.fromDomainToApi() =
    ApiFace(yaw, roll, quality, template)

fun FaceCapturePayload.Result.fromDomainToApi() = when (this) {
    FaceCapturePayload.Result.VALID -> VALID
    FaceCapturePayload.Result.INVALID -> INVALID
    FaceCapturePayload.Result.OFF_YAW -> OFF_YAW
    FaceCapturePayload.Result.OFF_ROLL -> OFF_ROLL
    FaceCapturePayload.Result.TOO_CLOSE -> TOO_CLOSE
    FaceCapturePayload.Result.TOO_FAR -> TOO_FAR
}
