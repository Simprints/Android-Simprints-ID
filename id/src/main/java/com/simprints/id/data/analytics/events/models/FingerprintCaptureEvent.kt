package com.simprints.id.data.analytics.events.models

import com.simprints.id.domain.Finger
import com.simprints.libsimprints.FingerIdentifier

class FingerprintCaptureEvent(val relativeStartTime: Long,
                              val relativeEndTime: Long,
                              val id: String,
                              val finger: FingerIdentifier,
                              val qualityThreshold: Int,
                              val result: Finger.Status,
                              val fingerprint: Fingerprint?): Event(EventType.FINGERPRINT_CAPTURE) {

    class Fingerprint(val quality: Int, val template: String)
}
