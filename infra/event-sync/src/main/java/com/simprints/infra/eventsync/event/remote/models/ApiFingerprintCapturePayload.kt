package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureEvent.FingerprintCapturePayload
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureEvent.FingerprintCapturePayload.Result.BAD_QUALITY
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureEvent.FingerprintCapturePayload.Result.FAILURE_TO_ACQUIRE
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureEvent.FingerprintCapturePayload.Result.GOOD_SCAN
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureEvent.FingerprintCapturePayload.Result.NO_FINGER_DETECTED
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureEvent.FingerprintCapturePayload.Result.SKIPPED
import com.simprints.infra.eventsync.event.remote.models.ApiFingerprintCapturePayload.ApiResult

@Keep
@JsonInclude(Include.NON_NULL)
internal data class ApiFingerprintCapturePayload(
    val id: String,
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val qualityThreshold: Int,
    val finger: IFingerIdentifier,
    val result: ApiResult,
    val fingerprint: ApiFingerprint?,
) : ApiEventPayload(startTime) {

    @Keep
    data class ApiFingerprint(
        val finger: IFingerIdentifier,
        val quality: Int,
        val format: String,
    ) {

        constructor(finger: FingerprintCapturePayload.Fingerprint) : this(
            finger.finger,
            finger.quality,
            finger.format
        )
    }

    constructor(domainPayload: FingerprintCapturePayload) : this(
        domainPayload.id,
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.endedAt?.fromDomainToApi(),
        domainPayload.qualityThreshold,
        domainPayload.finger,
        domainPayload.result.fromDomainToApi(),
        domainPayload.fingerprint?.let { ApiFingerprint(it) },
    )

    @Keep
    enum class ApiResult {

        GOOD_SCAN,
        BAD_QUALITY,
        NO_FINGER_DETECTED,
        SKIPPED,
        FAILURE_TO_ACQUIRE
    }

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? =
        null // this payload doesn't have tokenizable fields
}

internal fun FingerprintCapturePayload.Result.fromDomainToApi() =
    when (this) {
        GOOD_SCAN -> ApiResult.GOOD_SCAN
        BAD_QUALITY -> ApiResult.BAD_QUALITY
        NO_FINGER_DETECTED -> ApiResult.NO_FINGER_DETECTED
        SKIPPED -> ApiResult.SKIPPED
        FAILURE_TO_ACQUIRE -> ApiResult.FAILURE_TO_ACQUIRE
    }
