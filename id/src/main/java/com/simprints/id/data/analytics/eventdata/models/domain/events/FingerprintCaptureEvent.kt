package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.FingerIdentifier

@Keep
class FingerprintCaptureEvent(override val starTime: Long,
                              override val endTime: Long,
                              val qualityThreshold: Int,
                              val result: Result,
                              val fingerprint: Fingerprint?) : Event(EventType.FINGERPRINT_CAPTURE) {

    @Keep
    class Fingerprint(val finger: FingerIdentifier, val quality: Int, val template: String)

    @Keep
    enum class Result {
        GOOD_SCAN,
        BAD_QUALITY,
        NO_FINGER_DETECTED,
        SKIPPED,
        FAILURE_TO_ACQUIRE;
    }
}
