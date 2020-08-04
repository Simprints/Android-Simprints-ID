package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.FingerprintCaptureEvent.FingerprintCapturePayload
import com.simprints.id.data.db.event.domain.models.FingerprintCaptureEvent.FingerprintCapturePayload.Result.*
import com.simprints.id.data.db.event.remote.models.ApiFingerprintCapturePayload.ApiResult

@Keep
data class ApiFingerprintCapturePayload(val id: String,
                                        override val relativeStartTime: Long,
                                        override val version: Int,
                                        val relativeEndTime: Long,
                                        val qualityThreshold: Int,
                                        val finger: ApiFingerIdentifier,
                                        val result: ApiResult,
                                        val fingerprint: ApiFingerprint?) : ApiEventPayload(ApiEventPayloadType.FingerprintCapture, version, relativeStartTime) {

    @Keep
    data class ApiFingerprint(val finger: ApiFingerIdentifier, val quality: Int, val template: String) {

        constructor(finger: FingerprintCapturePayload.Fingerprint) : this(
            finger.finger.fromDomainToApi(),
            finger.quality, finger.template)
    }

    constructor(domainPayload: FingerprintCapturePayload) :
        this(domainPayload.id,
            domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.endedAt,
            domainPayload.qualityThreshold,
            domainPayload.finger.fromDomainToApi(),
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
