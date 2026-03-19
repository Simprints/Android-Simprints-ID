package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_VERIFICATION
import com.simprints.infra.events.event.domain.models.EventType.Companion.CALLBACK_VERIFICATION_KEY
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
@SerialName(CALLBACK_VERIFICATION_KEY)
data class VerificationCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: VerificationCallbackPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        score: CallbackComparisonScore,
    ) : this(
        UUID.randomUUID().toString(),
        VerificationCallbackPayload(createdAt, EVENT_VERSION, score),
        CALLBACK_VERIFICATION,
    )

    @Keep
    @Serializable
    data class VerificationCallbackPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val score: CallbackComparisonScore,
        override val endedAt: Timestamp? = null,
        override val type: EventType = CALLBACK_VERIFICATION,
    ) : EventPayload() {
        override fun toSafeString(): String = "confidence: ${score.confidence}"
    }

    companion object {
        const val EVENT_VERSION = 3
    }
}
