package com.simprints.id.data.analytics.eventData.models.events

import com.simprints.id.data.analytics.eventData.models.EventType
import com.simprints.id.domain.Finger
import com.simprints.libsimprints.FingerIdentifier

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

        companion object {
            fun fromFingerStatus(fingerStatus: Finger.Status): Result {
                return when (fingerStatus) {
                    Finger.Status.GOOD_SCAN, Finger.Status.RESCAN_GOOD_SCAN -> GOOD_SCAN
                    Finger.Status.BAD_SCAN -> BAD_QUALITY
                    Finger.Status.NO_FINGER_DETECTED -> NO_FINGER_DETECTED
                    Finger.Status.FINGER_SKIPPED -> SKIPPED
                    else -> FAILURE_TO_ACQUIRE
                }
            }
        }
    }
}
