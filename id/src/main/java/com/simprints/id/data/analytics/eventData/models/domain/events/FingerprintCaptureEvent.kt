package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.data.analytics.eventdata.models.domain.EventType
import com.simprints.id.FingerIdentifier
import com.simprints.id.activities.collectFingerprints.models.FingerStatus

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
            fun fromFingerStatus(fingerStatus: FingerStatus): Result {
                return when (fingerStatus) {
                    FingerStatus.GOOD_SCAN, FingerStatus.RESCAN_GOOD_SCAN -> GOOD_SCAN
                    FingerStatus.BAD_SCAN -> BAD_QUALITY
                    FingerStatus.NO_FINGER_DETECTED -> NO_FINGER_DETECTED
                    FingerStatus.FINGER_SKIPPED -> SKIPPED
                    else -> FAILURE_TO_ACQUIRE
                }
            }
        }
    }
}
