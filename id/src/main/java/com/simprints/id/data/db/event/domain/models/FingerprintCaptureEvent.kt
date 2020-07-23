package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.FINGERPRINT_CAPTURE
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import java.util.*

@Keep
class FingerprintCaptureEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val labels: MutableList<EventLabel>,
    override val payload: FingerprintCapturePayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        endTime: Long,
        finger: FingerIdentifier,
        qualityThreshold: Int,
        result: FingerprintCapturePayload.Result,
        fingerprint: FingerprintCapturePayload.Fingerprint?,
        id: String = UUID.randomUUID().toString(),
        sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
    ) : this(
        id,
        mutableListOf(SessionIdLabel(sessionId)),
        FingerprintCapturePayload(createdAt, EVENT_VERSION, endTime, finger, qualityThreshold, result, fingerprint, id),
        FINGERPRINT_CAPTURE)

    @Keep
    class FingerprintCapturePayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        override val endedAt: Long,
        val finger: FingerIdentifier,
        val qualityThreshold: Int,
        val result: Result,
        val fingerprint: Fingerprint?,
        val id: String
    ) : EventPayload(FINGERPRINT_CAPTURE, eventVersion, createdAt, endedAt) {

        @Keep
        data class Fingerprint(val finger: FingerIdentifier, val quality: Int, val template: String)

        @Keep
        enum class Result {
            GOOD_SCAN,
            BAD_QUALITY,
            NO_FINGER_DETECTED,
            SKIPPED,
            FAILURE_TO_ACQUIRE;
        }
    }

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
