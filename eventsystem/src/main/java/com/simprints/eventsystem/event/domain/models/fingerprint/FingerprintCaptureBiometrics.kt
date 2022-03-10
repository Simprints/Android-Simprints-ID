package com.simprints.eventsystem.event.domain.models.fingerprint

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventPayload
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import java.util.UUID

@Keep
data class FingerprintCaptureBiometrics(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: FingerprintCaptureBiometricsPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        finger: IFingerIdentifier,
        result: FingerprintCaptureBiometricsPayload.Result,
        fingerprint: FingerprintCaptureBiometricsPayload.Fingerprint?,
        id: String = UUID.randomUUID().toString(),
        labels: EventLabels = EventLabels()
    ) : this(
        id = id,
        labels = labels,
        payload = FingerprintCaptureBiometricsPayload(createdAt, EVENT_VERSION, finger, result, fingerprint, id),
        type = EventType.FINGERPRINT_CAPTURE_BIOMETRICS
    )

    @Keep
    data class FingerprintCaptureBiometricsPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val finger: IFingerIdentifier,
        val result: Result,
        val fingerprint: Fingerprint?,
        val id: String,
        override val type: EventType = EventType.FINGERPRINT_CAPTURE_BIOMETRICS,
        override val endedAt: Long = 0
    ) : EventPayload() {

        @Keep
        data class Fingerprint(
            val finger: IFingerIdentifier,
            val template: String,
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
        const val EVENT_VERSION = 0
    }
}
