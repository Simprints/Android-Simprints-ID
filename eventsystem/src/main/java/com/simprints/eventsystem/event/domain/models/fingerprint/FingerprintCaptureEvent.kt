package com.simprints.eventsystem.event.domain.models.fingerprint

import androidx.annotation.Keep
import com.simprints.core.tools.utils.randomUUID
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventPayload
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.EventType.FINGERPRINT_CAPTURE_V3
import com.simprints.moduleapi.fingerprint.IFingerIdentifier

@Keep
data class FingerprintCaptureEvent(
    override val id: String = randomUUID(),
    override var labels: EventLabels,
    override val payload: FingerprintCapturePayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        endTime: Long,
        finger: IFingerIdentifier,
        qualityThreshold: Int,
        result: FingerprintCapturePayload.Result,
        fingerprint: FingerprintCapturePayload.Fingerprint?,
        id: String = randomUUID(),
        labels: EventLabels = EventLabels(),
        payloadId: String = randomUUID()
    ) : this(
        id,
        labels,
        FingerprintCapturePayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            endedAt = endTime,
            finger = finger,
            qualityThreshold = qualityThreshold,
            result = result,
            fingerprint = fingerprint,
            id = payloadId
        ),
        FINGERPRINT_CAPTURE_V3
    )

    @Keep
    data class FingerprintCapturePayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        override var endedAt: Long,
        val finger: IFingerIdentifier,
        val qualityThreshold: Int,
        val result: Result,
        val fingerprint: Fingerprint?,
        val id: String,
        override val type: EventType = FINGERPRINT_CAPTURE_V3
    ) : EventPayload() {

        @Keep
        data class Fingerprint(
            val finger: IFingerIdentifier,
            val quality: Int,
            val format: FingerprintTemplateFormat = FingerprintTemplateFormat.ISO_19794_2
        )

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
        const val EVENT_VERSION = 3
    }
}
