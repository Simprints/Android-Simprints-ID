package com.simprints.infra.events.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType

@Keep
data class FaceCaptureBiometricsEvent(
    override val id: String = randomUUID(),
    override val payload: FaceCaptureBiometricsPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        startTime: Timestamp,
        face: FaceCaptureBiometricsPayload.Face,
        id: String = randomUUID(),
        payloadId: String = randomUUID(),
    ) : this(
        id,
        FaceCaptureBiometricsPayload(
            createdAt = startTime,
            eventVersion = EVENT_VERSION,
            face = face,
            id = payloadId,
        ),
        EventType.FACE_CAPTURE_BIOMETRICS,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class FaceCaptureBiometricsPayload(
        val id: String,
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val face: Face,
        override val endedAt: Timestamp? = null,
        override val type: EventType = EventType.FACE_CAPTURE_BIOMETRICS,
    ) : EventPayload() {
        override fun toSafeString(): String = "format: ${face.format}, quality: ${face.quality}"

        @Keep
        data class Face(
            val yaw: Float,
            var roll: Float,
            val template: String,
            val quality: Float,
            val format: String,
        )
    }

    companion object {
        const val EVENT_VERSION = 1
    }
}
