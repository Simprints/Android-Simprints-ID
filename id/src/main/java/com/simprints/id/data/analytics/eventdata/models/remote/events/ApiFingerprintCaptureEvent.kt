package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.FingerprintCaptureEvent

@Keep
class ApiFingerprintCaptureEvent(val id: String,
                                 val relativeStartTime: Long,
                                 val relativeEndTime: Long,
                                 val finger: ApiFingerIdentifier,
                                 val qualityThreshold: Int,
                                 val result: ApiResult,
                                 val fingerprint: ApiFingerprint?) : ApiEvent(ApiEventType.FINGERPRINT_CAPTURE) {

    @Keep
    class ApiFingerprint(val quality: Int, val template: String) {
        constructor(finger: FingerprintCaptureEvent.Fingerprint) : this(finger.quality, finger.template)
    }

    @Keep
    enum class ApiFingerIdentifier {
        RIGHT_5TH_FINGER,
        RIGHT_4TH_FINGER,
        RIGHT_3RD_FINGER,
        RIGHT_INDEX_FINGER,
        RIGHT_THUMB,
        LEFT_THUMB,
        LEFT_INDEX_FINGER,
        LEFT_3RD_FINGER,
        LEFT_4TH_FINGER,
        LEFT_5TH_FINGER
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
            fingerprintCaptureEvent.relativeStartTime,
            fingerprintCaptureEvent.relativeEndTime,
            ApiFingerIdentifier.valueOf(fingerprintCaptureEvent.finger.toString()),
            fingerprintCaptureEvent.qualityThreshold,
            ApiResult.valueOf(fingerprintCaptureEvent.result.toString()),
            fingerprintCaptureEvent.fingerprint?.let { ApiFingerprint(fingerprintCaptureEvent.fingerprint) })
}
