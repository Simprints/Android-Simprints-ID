package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep

import com.simprints.id.data.db.event.domain.models.EventType.FINGERPRINT_CAPTURE
import com.simprints.id.data.db.event.local.models.DbEvent.Companion.DEFAULT_EVENT_VERSION
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import java.util.*

@Keep
data class FingerprintCaptureEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: FingerprintCapturePayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        endTime: Long,
        finger: FingerIdentifier,
        qualityThreshold: Int,
        result: FingerprintCapturePayload.Result,
        fingerprint: FingerprintCapturePayload.Fingerprint?,
        id: String = UUID.randomUUID().toString(),
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        id,
        labels,
        FingerprintCapturePayload(createdAt, EVENT_VERSION, endTime, finger, qualityThreshold, result, fingerprint, id),
        FINGERPRINT_CAPTURE)

    @Keep
    data class FingerprintCapturePayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        override var endedAt: Long,
        val finger: FingerIdentifier,
        val qualityThreshold: Int,
        val result: Result,
        val fingerprint: Fingerprint?,
        val id: String,
        override val type: EventType = FINGERPRINT_CAPTURE
    ) : EventPayload() {

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
        const val EVENT_VERSION = 1
    }
}
