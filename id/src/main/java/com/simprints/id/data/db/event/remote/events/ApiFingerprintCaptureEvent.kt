package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.FingerprintCaptureEvent
import com.simprints.id.data.db.event.domain.events.FingerprintCaptureEvent.FingerprintCapturePayload
import com.simprints.id.data.db.event.domain.events.FingerprintCaptureEvent.FingerprintCapturePayload.Result.*
import com.simprints.id.data.db.event.remote.events.ApiFingerprintCaptureEvent.ApiFingerprintCapturePayload.ApiResult

@Keep
class ApiFingerprintCaptureEvent(domainEvent: FingerprintCaptureEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.map { it.fromDomainToApi() }.toMap(),
        domainEvent.payload.fromDomainToApi()) {


    @Keep
    class ApiFingerprintCapturePayload(val id: String,
                                       createdAt: Long,
                                       eventVersion: Int,
                                       val relativeEndTime: Long,
                                       val qualityThreshold: Int,
                                       val finger: ApiFingerIdentifier,
                                       val result: ApiResult,
                                       val fingerprint: ApiFingerprint?) : ApiEventPayload(ApiEventPayloadType.FINGERPRINT_CAPTURE, eventVersion, createdAt) {

        @Keep
        class ApiFingerprint(val finger: ApiFingerIdentifier, val quality: Int, val template: String) {

            constructor(finger: FingerprintCapturePayload.Fingerprint) : this(
                finger.finger.fromDomainToApi(),
                finger.quality, finger.template)
        }

        @Keep
        enum class ApiResult {
            GOOD_SCAN,
            BAD_QUALITY,
            NO_FINGER_DETECTED,
            SKIPPED,
            FAILURE_TO_ACQUIRE
        }

        constructor(domainPayload: FingerprintCapturePayload) :
            this(domainPayload.id,
                domainPayload.createdAt,
                domainPayload.eventVersion,
                domainPayload.endTime,
                domainPayload.qualityThreshold,
                domainPayload.finger.fromDomainToApi(),
                domainPayload.result.fromDomainToApi(),
                domainPayload.fingerprint?.let { ApiFingerprint(it) })
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
