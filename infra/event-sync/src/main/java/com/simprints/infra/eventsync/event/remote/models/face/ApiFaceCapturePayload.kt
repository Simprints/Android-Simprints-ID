package com.simprints.infra.eventsync.event.remote.models.face

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.face.FaceCaptureEvent.FaceCapturePayload
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayload
import com.simprints.infra.eventsync.event.remote.models.ApiTimestamp
import com.simprints.infra.eventsync.event.remote.models.face.ApiFaceCapturePayload.ApiFace
import com.simprints.infra.eventsync.event.remote.models.face.ApiFaceCapturePayload.ApiResult.BAD_QUALITY
import com.simprints.infra.eventsync.event.remote.models.face.ApiFaceCapturePayload.ApiResult.INVALID
import com.simprints.infra.eventsync.event.remote.models.face.ApiFaceCapturePayload.ApiResult.OFF_ROLL
import com.simprints.infra.eventsync.event.remote.models.face.ApiFaceCapturePayload.ApiResult.OFF_YAW
import com.simprints.infra.eventsync.event.remote.models.face.ApiFaceCapturePayload.ApiResult.TOO_CLOSE
import com.simprints.infra.eventsync.event.remote.models.face.ApiFaceCapturePayload.ApiResult.TOO_FAR
import com.simprints.infra.eventsync.event.remote.models.face.ApiFaceCapturePayload.ApiResult.VALID
import com.simprints.infra.eventsync.event.remote.models.fromDomainToApi

@Keep
@JsonInclude(Include.NON_NULL)
internal data class ApiFaceCapturePayload(
    val id: String,
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val attemptNb: Int,
    val qualityThreshold: Float,
    val result: ApiResult,
    val isFallback: Boolean,
    val face: ApiFace?,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: FaceCapturePayload) : this(
        domainPayload.id,
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.endedAt?.fromDomainToApi(),
        domainPayload.attemptNb,
        domainPayload.qualityThreshold,
        domainPayload.result.fromDomainToApi(),
        domainPayload.isFallback,
        domainPayload.face?.fromDomainToApi(),
    )

    @Keep
    data class ApiFace(
        val yaw: Float,
        var roll: Float,
        val quality: Float,
        val format: String,
    )

    @Keep
    enum class ApiResult {
        VALID,
        INVALID,
        BAD_QUALITY,
        OFF_YAW,
        OFF_ROLL,
        TOO_CLOSE,
        TOO_FAR,
    }

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}

internal fun FaceCapturePayload.Face.fromDomainToApi() = ApiFace(yaw, roll, quality, format)

internal fun FaceCapturePayload.Result.fromDomainToApi() = when (this) {
    FaceCapturePayload.Result.VALID -> VALID
    FaceCapturePayload.Result.INVALID -> INVALID
    FaceCapturePayload.Result.BAD_QUALITY -> BAD_QUALITY
    FaceCapturePayload.Result.OFF_YAW -> OFF_YAW
    FaceCapturePayload.Result.OFF_ROLL -> OFF_ROLL
    FaceCapturePayload.Result.TOO_CLOSE -> TOO_CLOSE
    FaceCapturePayload.Result.TOO_FAR -> TOO_FAR
}
