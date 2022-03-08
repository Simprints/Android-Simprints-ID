package com.simprints.eventsystem.event.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEventV3.FingerprintCapturePayloadV3
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEventV3.FingerprintCapturePayloadV3.Result.BAD_QUALITY
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEventV3.FingerprintCapturePayloadV3.Result.FAILURE_TO_ACQUIRE
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEventV3.FingerprintCapturePayloadV3.Result.GOOD_SCAN
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEventV3.FingerprintCapturePayloadV3.Result.NO_FINGER_DETECTED
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEventV3.FingerprintCapturePayloadV3.Result.SKIPPED
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.eventsystem.event.remote.models.ApiFingerprintCapturePayloadV3.ApiResult
import com.simprints.moduleapi.fingerprint.IFingerIdentifier

@Keep
@JsonInclude(Include.NON_NULL)
data class ApiFingerprintCapturePayloadV3(
    val id: String,
    override val startTime: Long,
    override val version: Int,
    val endTime: Long,
    val qualityThreshold: Int,
    val finger: IFingerIdentifier,
    val result: ApiResult,
    val fingerprint: ApiFingerprint?
) : ApiEventPayload(ApiEventPayloadType.FingerprintCapture, version, startTime) {

    @Keep
    data class ApiFingerprint(
        val finger: IFingerIdentifier,
        val quality: Int,
        val format: FingerprintTemplateFormat
    ) {

        constructor(finger: FingerprintCapturePayloadV3.Fingerprint) : this(
            finger.finger,
            finger.quality,
            finger.format
        )
    }

    constructor(domainPayload: FingerprintCapturePayloadV3) :
        this(domainPayload.id,
            domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.endedAt,
            domainPayload.qualityThreshold,
            domainPayload.finger,
            domainPayload.result.fromDomainToApi(),
            domainPayload.fingerprint?.let { ApiFingerprint(it) })

    @Keep
    enum class ApiResult {
        GOOD_SCAN,
        BAD_QUALITY,
        NO_FINGER_DETECTED,
        SKIPPED,
        FAILURE_TO_ACQUIRE
    }

}

fun FingerprintCapturePayloadV3.Result.fromDomainToApi() =
    when (this) {
        GOOD_SCAN -> ApiResult.GOOD_SCAN
        BAD_QUALITY -> ApiResult.BAD_QUALITY
        NO_FINGER_DETECTED -> ApiResult.NO_FINGER_DETECTED
        SKIPPED -> ApiResult.SKIPPED
        FAILURE_TO_ACQUIRE -> ApiResult.FAILURE_TO_ACQUIRE
    }
