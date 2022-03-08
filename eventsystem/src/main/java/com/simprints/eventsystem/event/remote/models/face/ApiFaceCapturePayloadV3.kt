package com.simprints.eventsystem.event.remote.models.face

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEventV3
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEventV3.FaceCapturePayloadV3
import com.simprints.eventsystem.event.domain.models.face.FaceTemplateFormat
import com.simprints.eventsystem.event.remote.models.ApiEventPayload
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.FaceCapture
import com.simprints.eventsystem.event.remote.models.face.ApiFaceCapturePayloadV3.ApiFace
import com.simprints.eventsystem.event.remote.models.face.ApiFaceCapturePayloadV3.ApiResult.INVALID
import com.simprints.eventsystem.event.remote.models.face.ApiFaceCapturePayloadV3.ApiResult.OFF_ROLL
import com.simprints.eventsystem.event.remote.models.face.ApiFaceCapturePayloadV3.ApiResult.OFF_YAW
import com.simprints.eventsystem.event.remote.models.face.ApiFaceCapturePayloadV3.ApiResult.TOO_CLOSE
import com.simprints.eventsystem.event.remote.models.face.ApiFaceCapturePayloadV3.ApiResult.TOO_FAR
import com.simprints.eventsystem.event.remote.models.face.ApiFaceCapturePayloadV3.ApiResult.VALID

@Keep
@JsonInclude(Include.NON_NULL)
data class ApiFaceCapturePayloadV3(
    val id: String,
    override val startTime: Long,
    val endTime: Long,
    override val version: Int,
    val attemptNb: Int,
    val qualityThreshold: Float,
    val result: ApiResult,
    val isFallback: Boolean,
    val face: ApiFace?
) : ApiEventPayload(FaceCapture, version, startTime) {

    constructor(domainPayload: FaceCapturePayloadV3) : this(
        domainPayload.id,
        domainPayload.createdAt,
        domainPayload.endedAt,
        domainPayload.eventVersion,
        domainPayload.attemptNb,
        domainPayload.qualityThreshold,
        domainPayload.result.fromDomainToApi(),
        domainPayload.isFallback,
        domainPayload.face?.fromDomainToApi()
    )

    @Keep
    data class ApiFace(
        val yaw: Float,
        var roll: Float,
        val quality: Float,
        val format: FaceTemplateFormat
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

fun FaceCapturePayloadV3.Face.fromDomainToApi() = ApiFace(yaw, roll, quality, format)

fun FaceCapturePayloadV3.Result.fromDomainToApi() = when (this) {
    FaceCapturePayloadV3.Result.VALID -> VALID
    FaceCapturePayloadV3.Result.INVALID -> INVALID
    FaceCapturePayloadV3.Result.OFF_YAW -> OFF_YAW
    FaceCapturePayloadV3.Result.OFF_ROLL -> OFF_ROLL
    FaceCapturePayloadV3.Result.TOO_CLOSE -> TOO_CLOSE
    FaceCapturePayloadV3.Result.TOO_FAR -> TOO_FAR
}
