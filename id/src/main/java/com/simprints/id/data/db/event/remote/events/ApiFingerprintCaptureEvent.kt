package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.FingerprintCaptureEvent
import com.simprints.id.data.db.event.domain.events.FingerprintCaptureEvent.FingerprintCapturePayload

@Keep
class ApiFingerprintCaptureEvent(val id: String,
                                 val relativeStartTime: Long,
                                 val relativeEndTime: Long,
                                 val qualityThreshold: Int,
                                 val finger: ApiFingerIdentifier,
                                 val result: ApiResult,
                                 val fingerprint: ApiFingerprint?) : ApiEvent(ApiEventType.FINGERPRINT_CAPTURE) {

    @Keep
    class ApiFingerprint(val finger: ApiFingerIdentifier, val quality: Int, val template: String) {

        constructor(finger: FingerprintCapturePayload.Fingerprint) : this(
            ApiFingerIdentifier.valueOf(finger.finger.toString()),
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

    constructor(fingerprintCaptureEvent: FingerprintCaptureEvent) :
        this(fingerprintCaptureEvent.id,
            (fingerprintCaptureEvent.payload as FingerprintCapturePayload).creationTime,
            fingerprintCaptureEvent.payload.endTime,
            fingerprintCaptureEvent.payload.qualityThreshold,
            fingerprintCaptureEvent.payload.finger.toApiFingerIdentifier(),
            ApiResult.valueOf(fingerprintCaptureEvent.payload.result.toString()),
            fingerprintCaptureEvent.payload.fingerprint?.let { ApiFingerprint(it) })
}
