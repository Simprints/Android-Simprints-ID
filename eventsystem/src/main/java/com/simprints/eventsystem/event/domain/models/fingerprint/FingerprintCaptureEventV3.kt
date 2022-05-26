package com.simprints.eventsystem.event.domain.models.fingerprint

import androidx.annotation.Keep
import com.simprints.core.tools.utils.randomUUID
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventPayload
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.EventType.FINGERPRINT_CAPTURE_V3
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import java.util.UUID

/*The naming here (3 instead of 2) is to keep in line with the backend, which is at V3 of this. See EVENT_VERSION in the companion
* object, which also needed incrementing*/
@Keep
data class FingerprintCaptureEventV3(
    override val id: String = randomUUID(),
    override var labels: EventLabels,
    override val payload: FingerprintCapturePayloadV3,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        endTime: Long,
        finger: IFingerIdentifier,
        qualityThreshold: Int,
        result: FingerprintCapturePayloadV3.Result,
        fingerprint: FingerprintCapturePayloadV3.Fingerprint?,
        id: String = randomUUID(),
        labels: EventLabels = EventLabels()
    ) : this(
        id,
        labels,
        FingerprintCapturePayloadV3(createdAt, EVENT_VERSION, endTime, finger, qualityThreshold, result, fingerprint),
        FINGERPRINT_CAPTURE_V3
    )

    @Keep
    data class FingerprintCapturePayloadV3(
        override val createdAt: Long,
        override val eventVersion: Int,
        override var endedAt: Long,
        val finger: IFingerIdentifier,
        val qualityThreshold: Int,
        val result: Result,
        val fingerprint: Fingerprint?,
        val id: String = FINGERPRINT_CAPTURE_ID,
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
        val FINGERPRINT_CAPTURE_ID = UUID.randomUUID().toString()
    }
}
