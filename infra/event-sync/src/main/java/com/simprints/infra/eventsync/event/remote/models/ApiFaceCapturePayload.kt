package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.FaceCaptureEvent.FaceCapturePayload
import com.simprints.infra.eventsync.event.remote.models.ApiFaceCapturePayload.ApiFace
import com.simprints.infra.eventsync.event.remote.models.ApiFaceCapturePayload.ApiResult.BAD_QUALITY
import com.simprints.infra.eventsync.event.remote.models.ApiFaceCapturePayload.ApiResult.INVALID
import com.simprints.infra.eventsync.event.remote.models.ApiFaceCapturePayload.ApiResult.OFF_ROLL
import com.simprints.infra.eventsync.event.remote.models.ApiFaceCapturePayload.ApiResult.OFF_YAW
import com.simprints.infra.eventsync.event.remote.models.ApiFaceCapturePayload.ApiResult.TOO_CLOSE
import com.simprints.infra.eventsync.event.remote.models.ApiFaceCapturePayload.ApiResult.TOO_FAR
import com.simprints.infra.eventsync.event.remote.models.ApiFaceCapturePayload.ApiResult.VALID
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiFaceCapturePayload(
    val id: String,
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val attemptNb: Int,
    val qualityThreshold: Float,
    val result: ApiResult,
    val isFallback: Boolean,
    val face: ApiFace?,
) : ApiEventPayload() {
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
    @Serializable
    data class ApiFace(
        val yaw: Float,
        var roll: Float,
        val quality: Float,
        val format: String,
    )

    @Keep
    @Serializable
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
