package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.FingerprintCaptureEvent

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

        constructor(finger: FingerprintCaptureEvent.Fingerprint) : this(
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
            fingerprintCaptureEvent.relativeStartTime ?: 0,
            fingerprintCaptureEvent.relativeEndTime ?: 0,
            fingerprintCaptureEvent.qualityThreshold,
            fingerprintCaptureEvent.finger.toApiFingerIdentifier(),
            ApiResult.valueOf(fingerprintCaptureEvent.result.toString()),
            fingerprintCaptureEvent.fingerprint?.let { ApiFingerprint(fingerprintCaptureEvent.fingerprint) })
}
