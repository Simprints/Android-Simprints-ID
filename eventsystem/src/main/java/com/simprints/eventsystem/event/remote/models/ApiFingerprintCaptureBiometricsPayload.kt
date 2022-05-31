package com.simprints.eventsystem.event.remote.models

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.moduleapi.fingerprint.IFingerIdentifier

@Keep
data class ApiFingerprintCaptureBiometricsPayload(
    override val version: Int,
    override val startTime: Long,
    val result: ApiResult,
    val fingerprint: Fingerprint?,
    val qualityThreshold: Int,
    val id: String,
) : ApiEventPayload(ApiEventPayloadType.FingerprintCaptureBiometrics, version, startTime) {

    @Keep
    data class Fingerprint(
        val finger: IFingerIdentifier,
        val template: String,
        val format: FingerprintTemplateFormat
    ) {
        constructor(finger: FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Fingerprint) : this(
            finger.finger,
            finger.template,
            finger.format
        )
    }

    constructor(domainPayload: FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload) : this(
        domainPayload.eventVersion,
        domainPayload.createdAt,
        domainPayload.result.fromDomainToApi(),
        domainPayload.fingerprint?.let { Fingerprint(it) },
        domainPayload.qualityThreshold,
        domainPayload.id
    )

    @Keep
    enum class ApiResult {
        GOOD_SCAN,
        BAD_QUALITY,
        NO_FINGER_DETECTED,
        SKIPPED,
        FAILURE_TO_ACQUIRE
    }
}

fun FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Result.fromDomainToApi() =
    when (this) {
        FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Result.GOOD_SCAN -> ApiFingerprintCaptureBiometricsPayload.ApiResult.GOOD_SCAN
        FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Result.BAD_QUALITY -> ApiFingerprintCaptureBiometricsPayload.ApiResult.BAD_QUALITY
        FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Result.NO_FINGER_DETECTED -> ApiFingerprintCaptureBiometricsPayload.ApiResult.NO_FINGER_DETECTED
        FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Result.SKIPPED -> ApiFingerprintCaptureBiometricsPayload.ApiResult.SKIPPED
        FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Result.FAILURE_TO_ACQUIRE -> ApiFingerprintCaptureBiometricsPayload.ApiResult.FAILURE_TO_ACQUIRE
    }

