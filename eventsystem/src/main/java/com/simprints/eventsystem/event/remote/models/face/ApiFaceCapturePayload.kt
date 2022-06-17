package com.simprints.eventsystem.event.remote.models.face

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEvent.FaceCapturePayload
import com.simprints.eventsystem.event.domain.models.face.FaceTemplateFormat
import com.simprints.eventsystem.event.remote.models.ApiEventPayload
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.FaceCapture
import com.simprints.eventsystem.event.remote.models.face.ApiFaceCapturePayload.ApiFace
import com.simprints.eventsystem.event.remote.models.face.ApiFaceCapturePayload.ApiResult.INVALID
import com.simprints.eventsystem.event.remote.models.face.ApiFaceCapturePayload.ApiResult.OFF_ROLL
import com.simprints.eventsystem.event.remote.models.face.ApiFaceCapturePayload.ApiResult.OFF_YAW
import com.simprints.eventsystem.event.remote.models.face.ApiFaceCapturePayload.ApiResult.TOO_CLOSE
import com.simprints.eventsystem.event.remote.models.face.ApiFaceCapturePayload.ApiResult.TOO_FAR
import com.simprints.eventsystem.event.remote.models.face.ApiFaceCapturePayload.ApiResult.VALID

@Keep
@JsonInclude(Include.NON_NULL)
data class ApiFaceCapturePayload(
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

    constructor(domainPayload: FaceCapturePayload) : this(
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

fun FaceCapturePayload.Face.fromDomainToApi() = ApiFace(yaw, roll, quality, format)

fun FaceCapturePayload.Result.fromDomainToApi() = when (this) {
    FaceCapturePayload.Result.VALID -> VALID
    FaceCapturePayload.Result.INVALID -> INVALID
    FaceCapturePayload.Result.OFF_YAW -> OFF_YAW
    FaceCapturePayload.Result.OFF_ROLL -> OFF_ROLL
    FaceCapturePayload.Result.TOO_CLOSE -> TOO_CLOSE
    FaceCapturePayload.Result.TOO_FAR -> TOO_FAR
}
