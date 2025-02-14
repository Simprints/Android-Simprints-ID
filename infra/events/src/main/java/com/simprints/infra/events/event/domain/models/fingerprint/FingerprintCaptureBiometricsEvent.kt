package com.simprints.infra.events.event.domain.models.fingerprint

import androidx.annotation.Keep
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType

@Keep
data class FingerprintCaptureBiometricsEvent(
    override val id: String = randomUUID(),
    override val payload: FingerprintCaptureBiometricsPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        fingerprint: FingerprintCaptureBiometricsPayload.Fingerprint,
        id: String = randomUUID(),
        payloadId: String = randomUUID(),
    ) : this(
        id = id,
        payload = FingerprintCaptureBiometricsPayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            fingerprint = fingerprint,
            id = payloadId,
        ),
        type = EventType.FINGERPRINT_CAPTURE_BIOMETRICS,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class FingerprintCaptureBiometricsPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val fingerprint: Fingerprint,
        val id: String,
        override val endedAt: Timestamp? = null,
        override val type: EventType = EventType.FINGERPRINT_CAPTURE_BIOMETRICS,
    ) : EventPayload() {
        override fun toSafeString(): String = "format: ${fingerprint.format}, quality: ${fingerprint.quality}"

        @Keep
        data class Fingerprint(
            val finger: IFingerIdentifier,
            val template: String,
            val quality: Int,
            val format: String,
        )
    }

    companion object {
        const val EVENT_VERSION = 1
    }
}
