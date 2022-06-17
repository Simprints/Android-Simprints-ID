package com.simprints.eventsystem.event.remote.models

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.moduleapi.fingerprint.IFingerIdentifier

@Keep
data class ApiFingerprintCaptureBiometricsPayload(
    override val version: Int,
    override val startTime: Long,
    val fingerprint: Fingerprint,
    val id: String,
) : ApiEventPayload(ApiEventPayloadType.FingerprintCaptureBiometrics, version, startTime) {

    @Keep
    data class Fingerprint(
        val finger: IFingerIdentifier,
        val template: String,
        val quality: Int,
        val format: FingerprintTemplateFormat
    ) {
        constructor(finger: FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Fingerprint) : this(
            finger.finger,
            finger.template,
            finger.quality,
            finger.format
        )
    }

    constructor(domainPayload: FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload) : this(
        domainPayload.eventVersion,
        domainPayload.createdAt,
        Fingerprint(domainPayload.fingerprint),
        domainPayload.id
    )
}

