package com.simprints.fingerprint.tools.extensions

import com.simprints.fingerprint.activities.collect.models.FingerStatus
import com.simprints.fingerprint.activities.collect.models.FingerStatus.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.FingerprintCaptureEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.FingerprintCaptureEvent.Result

fun FingerStatus.toResultEvent(): Result {
    return when (this) {
        GOOD_SCAN, RESCAN_GOOD_SCAN -> FingerprintCaptureEvent.Result.GOOD_SCAN
        BAD_SCAN -> FingerprintCaptureEvent.Result.BAD_QUALITY
        NO_FINGER_DETECTED -> FingerprintCaptureEvent.Result.NO_FINGER_DETECTED
        FINGER_SKIPPED -> FingerprintCaptureEvent.Result.SKIPPED
        else -> FingerprintCaptureEvent.Result.FAILURE_TO_ACQUIRE
    }
}
