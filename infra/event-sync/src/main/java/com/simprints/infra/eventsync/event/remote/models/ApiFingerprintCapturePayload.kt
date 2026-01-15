package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.FingerprintCaptureEvent.FingerprintCapturePayload
import com.simprints.infra.events.event.domain.models.FingerprintCaptureEvent.FingerprintCapturePayload.Result.BAD_QUALITY
import com.simprints.infra.events.event.domain.models.FingerprintCaptureEvent.FingerprintCapturePayload.Result.FAILURE_TO_ACQUIRE
import com.simprints.infra.events.event.domain.models.FingerprintCaptureEvent.FingerprintCapturePayload.Result.GOOD_SCAN
import com.simprints.infra.events.event.domain.models.FingerprintCaptureEvent.FingerprintCapturePayload.Result.NO_FINGER_DETECTED
import com.simprints.infra.events.event.domain.models.FingerprintCaptureEvent.FingerprintCapturePayload.Result.SKIPPED
import com.simprints.infra.eventsync.event.remote.models.ApiFingerprintCapturePayload.ApiResult
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiFingerprintCapturePayload(
    val id: String,
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val qualityThreshold: Int,
    val finger: TemplateIdentifier,
    val result: ApiResult,
    val fingerprint: ApiFingerprint?,
) : ApiEventPayload() {
    @Keep
    @Serializable
    data class ApiFingerprint(
        val finger: TemplateIdentifier,
        val quality: Int,
        val format: String,
    ) {
        constructor(finger: FingerprintCapturePayload.Fingerprint) : this(
            finger.finger,
            finger.quality,
            finger.format,
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
    @Serializable
    enum class ApiResult {
        GOOD_SCAN,
        BAD_QUALITY,
        NO_FINGER_DETECTED,
        SKIPPED,
        FAILURE_TO_ACQUIRE,
    }

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}

internal fun FingerprintCapturePayload.Result.fromDomainToApi() = when (this) {
    GOOD_SCAN -> ApiResult.GOOD_SCAN
    BAD_QUALITY -> ApiResult.BAD_QUALITY
    NO_FINGER_DETECTED -> ApiResult.NO_FINGER_DETECTED
    SKIPPED -> ApiResult.SKIPPED
    FAILURE_TO_ACQUIRE -> ApiResult.FAILURE_TO_ACQUIRE
}
