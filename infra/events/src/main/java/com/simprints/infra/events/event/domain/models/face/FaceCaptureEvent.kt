package com.simprints.infra.events.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.FACE_CAPTURE
import com.simprints.infra.events.event.domain.models.face.FaceCaptureEvent.FaceCapturePayload.Face

@Keep
data class FaceCaptureEvent(
    override val id: String = randomUUID(),
    override val payload: FaceCapturePayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        startTime: Timestamp,
        endTime: Timestamp,
        attemptNb: Int,
        qualityThreshold: Float,
        result: FaceCapturePayload.Result,
        isAutoCapture: Boolean,
        isFallback: Boolean,
        face: Face?,
        id: String = randomUUID(),
        payloadId: String = randomUUID(),
    ) : this(
        id,
        FaceCapturePayload(
            createdAt = startTime,
            endedAt = endTime,
            eventVersion = EVENT_VERSION,
            attemptNb = attemptNb,
            qualityThreshold = qualityThreshold,
            result = result,
            isAutoCapture = isAutoCapture,
            isFallback = isFallback,
            face = face,
            id = payloadId,
        ),
        FACE_CAPTURE,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class FaceCapturePayload(
        val id: String,
        override val createdAt: Timestamp,
        override var endedAt: Timestamp?,
        override val eventVersion: Int,
        val attemptNb: Int,
        val qualityThreshold: Float,
        val result: Result,
        val isAutoCapture: Boolean,
        val isFallback: Boolean,
        val face: Face?,
        override val type: EventType = FACE_CAPTURE,
    ) : EventPayload() {
        override fun toSafeString(): String = "result: $result, attempt nr: $attemptNb, auto-capture: $isAutoCapture, fallback: $isFallback, " +
            "quality: ${face?.quality},  format: ${face?.format}"

        @Keep
        data class Face(
            val yaw: Float,
            var roll: Float,
            val quality: Float,
            val format: String,
        )

        enum class Result {
            VALID,
            INVALID,
            BAD_QUALITY,
            OFF_YAW,
            OFF_ROLL,
            TOO_CLOSE,
            TOO_FAR,
        }
    }

    companion object {
        const val EVENT_VERSION = 4
    }
}
