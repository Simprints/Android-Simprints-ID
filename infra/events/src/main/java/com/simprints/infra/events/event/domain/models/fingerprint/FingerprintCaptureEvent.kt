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
import com.simprints.infra.events.event.domain.models.EventType.FINGERPRINT_CAPTURE

@Keep
data class FingerprintCaptureEvent(
    override val id: String = randomUUID(),
    override val payload: FingerprintCapturePayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        endTime: Timestamp,
        finger: IFingerIdentifier,
        qualityThreshold: Int,
        result: FingerprintCapturePayload.Result,
        fingerprint: FingerprintCapturePayload.Fingerprint?,
        id: String = randomUUID(),
        payloadId: String = randomUUID(),
    ) : this(
        id,
        FingerprintCapturePayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            endedAt = endTime,
            finger = finger,
            qualityThreshold = qualityThreshold,
            result = result,
            fingerprint = fingerprint,
            id = payloadId,
        ),
        FINGERPRINT_CAPTURE,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class FingerprintCapturePayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        override var endedAt: Timestamp?,
        val finger: IFingerIdentifier,
        val qualityThreshold: Int,
        val result: Result,
        val fingerprint: Fingerprint?,
        val id: String,
        override val type: EventType = FINGERPRINT_CAPTURE,
    ) : EventPayload() {
        override fun toSafeString(): String = "finger: $finger, result: $result, " +
            "quality: ${fingerprint?.quality}, format: ${fingerprint?.format}"

        @Keep
        data class Fingerprint(
            val finger: IFingerIdentifier,
            val quality: Int,
            val format: String,
        )

        @Keep
        enum class Result {
            GOOD_SCAN,
            BAD_QUALITY,
            NO_FINGER_DETECTED,
            SKIPPED,
            FAILURE_TO_ACQUIRE,
        }
    }

    companion object {
        const val EVENT_VERSION = 4
    }
}
