package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.fingerprint.data.domain.fingerprint.fromDomainToModuleApi
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent as FingerprintCaptureBiometricsEventCore

@Keep
class FingerprintCaptureBiometricsEvent(
    createdAt: Long,
    endedAt: Long = 0,
    val fingerprint: Fingerprint,
    val payloadId: String
) : Event(
    type = EventType.FINGERPRINT_CAPTURE_BIOMETRICS,
    startTime = createdAt,
    endTime = endedAt
) {

    @Keep
    class Fingerprint(
        val finger: FingerIdentifier,
        val quality: Int,
        val template: String,
        val format: FingerprintTemplateFormat = FingerprintTemplateFormat.ISO_19794_2
    )
}

fun FingerprintCaptureBiometricsEvent.fromDomainToCore() = FingerprintCaptureBiometricsEventCore(
    createdAt = startTime,
    fingerprint = fingerprint.fromDomainToCore(),
    payloadId = payloadId
)

fun FingerprintCaptureBiometricsEvent.Fingerprint.fromDomainToCore() =
    FingerprintCaptureBiometricsEventCore.FingerprintCaptureBiometricsPayload.Fingerprint(
        finger = finger.fromDomainToModuleApi(),
        template = template,
        quality = quality,
        format = format
    )
