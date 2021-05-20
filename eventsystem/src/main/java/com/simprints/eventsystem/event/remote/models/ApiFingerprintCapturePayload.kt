package com.simprints.eventsystem.event.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEvent.FingerprintCapturePayload
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEvent.FingerprintCapturePayload.Result.*
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.eventsystem.event.remote.models.ApiFingerprintCapturePayload.ApiResult
import com.simprints.moduleapi.fingerprint.IFingerIdentifier

@Keep
@JsonInclude(Include.NON_NULL)
data class ApiFingerprintCapturePayload(
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
        val template: String,
        val format: FingerprintTemplateFormat
    ) {

        constructor(finger: FingerprintCapturePayload.Fingerprint) : this(
            finger.finger,
            finger.quality,
            finger.template,
            finger.format
        )
    }

    constructor(domainPayload: FingerprintCapturePayload) :
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


fun FingerprintCapturePayload.Result.fromDomainToApi() =
    when (this) {
        GOOD_SCAN -> ApiResult.GOOD_SCAN
        BAD_QUALITY -> ApiResult.BAD_QUALITY
        NO_FINGER_DETECTED -> ApiResult.NO_FINGER_DETECTED
        SKIPPED -> ApiResult.SKIPPED
        FAILURE_TO_ACQUIRE -> ApiResult.FAILURE_TO_ACQUIRE
    }
