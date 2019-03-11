package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.FingerIdentifier

class FingerprintCaptureEvent(val relativeStartTime: Long,
                              val relativeEndTime: Long,
                              val finger: FingerIdentifier,
                              val qualityThreshold: Int,
                              val result: Result,
                              val fingerprint: Fingerprint?) : Event(EventType.FINGERPRINT_CAPTURE) {

    class Fingerprint(val quality: Int, val template: String)

    enum class Result {
        GOOD_SCAN,
        BAD_QUALITY,
        NO_FINGER_DETECTED,
        SKIPPED,
        FAILURE_TO_ACQUIRE;
    }
}
