package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import java.util.*

@Keep
class FingerprintCaptureEvent(
    startTime: Long,
    endTime: Long,
    finger: FingerIdentifier,
    qualityThreshold: Int,
    result: FingerprintCapturePayload.Result,
    fingerprint: FingerprintCapturePayload.Fingerprint?,
    id: String = UUID.randomUUID().toString(),
    sessionId: String = UUID.randomUUID().toString(), //StopShip: to change in PAS-993
    sessionStartTime: Long = 0 //StopShip: to change in PAS-993
) : Event(
    id,
    listOf(EventLabel.SessionId(sessionId)),
    FingerprintCapturePayload(startTime, startTime - sessionStartTime, endTime, endTime - sessionStartTime, finger, qualityThreshold, result, fingerprint, id)) {

    @Keep
    class FingerprintCapturePayload(
        startTime: Long,
        relativeStartTime: Long,
        val endTime: Long,
        val relativeEndTime: Long,
        val finger: FingerIdentifier,
        val qualityThreshold: Int,
        val result: Result,
        val fingerprint: Fingerprint?,
        val id: String
    ) : EventPayload(EventPayloadType.FINGERPRINT_CAPTURE, startTime, relativeStartTime) {

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
}
