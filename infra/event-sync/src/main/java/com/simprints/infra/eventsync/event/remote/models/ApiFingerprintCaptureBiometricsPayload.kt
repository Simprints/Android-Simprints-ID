package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.core.domain.fingerprint.IFingerIdentifier

@Keep
internal data class ApiFingerprintCaptureBiometricsPayload(
    override val version: Int,
    override val startTime: ApiTimestamp,
    val fingerprint: Fingerprint,
    val id: String,
) : ApiEventPayload(version, startTime) {

    @Keep
    data class Fingerprint(
        val finger: IFingerIdentifier,
        val template: String,
        val quality: Int,
        val format: String,
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
        domainPayload.createdAt.fromDomainToApi(),
        Fingerprint(domainPayload.fingerprint),
        domainPayload.id
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? =
        null // this payload doesn't have tokenizable fields
}

