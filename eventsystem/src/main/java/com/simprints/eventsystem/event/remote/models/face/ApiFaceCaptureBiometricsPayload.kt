package com.simprints.eventsystem.event.remote.models.face

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.eventsystem.event.domain.models.face.FaceTemplateFormat
import com.simprints.eventsystem.event.remote.models.ApiEventPayload
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType

@Keep
class ApiFaceCaptureBiometricsPayload(
    override val version: Int,
    override val startTime: Long,
    val id: String,
    val result: ApiResult,
    val qualityThreshold: Float,
    val face: Face?
) : ApiEventPayload(ApiEventPayloadType.FaceCaptureBiometrics, version, startTime) {

    @Keep
    data class Face(
        val template: String,
        val format: FaceTemplateFormat
    ) {
        constructor(face: FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Face) : this(
            face.template,
            face.format
        )
    }

    constructor(domainPayload: FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload) : this(
        domainPayload.eventVersion,
        domainPayload.createdAt,
        domainPayload.id,
        domainPayload.result.fromDomainToApi(),
        domainPayload.qualityThreshold,
        domainPayload.face?.let { Face(it) }
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

fun FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Result.fromDomainToApi() =
    when (this) {
        FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Result.VALID -> ApiFaceCaptureBiometricsPayload.ApiResult.VALID
        FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Result.INVALID -> ApiFaceCaptureBiometricsPayload.ApiResult.INVALID
        FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Result.OFF_YAW -> ApiFaceCaptureBiometricsPayload.ApiResult.OFF_YAW
        FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Result.OFF_ROLL -> ApiFaceCaptureBiometricsPayload.ApiResult.OFF_ROLL
        FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Result.TOO_CLOSE -> ApiFaceCaptureBiometricsPayload.ApiResult.TOO_CLOSE
        FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Result.TOO_FAR -> ApiFaceCaptureBiometricsPayload.ApiResult.TOO_FAR
    }
